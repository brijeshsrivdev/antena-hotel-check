package com.antenapro.hotelcheck.sitecheck.fetch;

import com.antenapro.hotelcheck.sitecheck.SiteCheckProperties;
import com.antenapro.hotelcheck.sitecheck.html.BookingCta;
import com.antenapro.hotelcheck.sitecheck.html.HtmlSupport;
import com.antenapro.hotelcheck.sitecheck.html.PageLinks;
import com.antenapro.hotelcheck.sitecheck.model.FetchedPage;
import com.antenapro.hotelcheck.sitecheck.model.PageRole;
import com.antenapro.hotelcheck.sitecheck.model.PsiResult;
import com.antenapro.hotelcheck.sitecheck.model.RobotsResult;
import com.antenapro.hotelcheck.sitecheck.model.SiteSnapshot;
import com.antenapro.hotelcheck.sitecheck.model.SitemapResult;
import com.antenapro.hotelcheck.sitecheck.psi.PsiClient;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Builds the snapshot for one run. The PageSpeed Insights call and the robots.txt/sitemap reads start at once
 * on the side executor (a Lighthouse run takes 10–40 s); the pages are fetched meanwhile — the home page,
 * then up to four more discovered on it (rooms, contact, the booking CTA's target, about), in that order,
 * stopping at the page budget or the fetch deadline; then a handful of other internal links are probed for
 * the broken-links check. Side tasks still running at the deadline become limitations; a PSI call keeps
 * running so its result is cached for the host's next run.
 */
public class SiteSnapshotLoader {

    private static final Logger log = LoggerFactory.getLogger(SiteSnapshotLoader.class);
    private static final List<PageRole> DISCOVERY_ORDER = List.of(PageRole.ROOMS, PageRole.CONTACT, PageRole.BOOKING, PageRole.ABOUT);

    private record RobotsAndSitemap(RobotsResult robots, SitemapResult sitemap, List<FetchedPage> fetches) {
    }

    private final SiteCheckProperties properties;
    private final PageFetcher fetcher;
    private final UrlPolicy policy;
    private final PsiClient psiClient;
    private final ExecutorService sideTasks;
    private final Clock clock;

    public SiteSnapshotLoader(SiteCheckProperties properties, PageFetcher fetcher, UrlPolicy policy, PsiClient psiClient,
                              ExecutorService sideTasks, Clock clock) {
        this.properties = properties;
        this.fetcher = fetcher;
        this.policy = policy;
        this.psiClient = psiClient;
        this.sideTasks = sideTasks;
        this.clock = clock;
    }

    /**
     * @param deadline no new fetch starts after this instant; pages left unfetched become a limitation
     */
    public SiteSnapshot load(URI requestedUrl, Instant deadline) {
        Future<PsiResult> psiFuture = sideTasks.submit(() -> psiClient.analyze(requestedUrl));
        Future<RobotsAndSitemap> robotsFuture = sideTasks.submit(() -> loadRobotsAndSitemap(requestedUrl));

        Map<PageRole, FetchedPage> pages = new EnumMap<>(PageRole.class);
        List<FetchedPage> fetched = new ArrayList<>();
        List<String> limitations = new ArrayList<>();

        FetchedPage home = fetcher.fetch(requestedUrl, PageRole.HOME);
        pages.put(PageRole.HOME, home);
        fetched.add(home);
        if (!home.ok()) {
            limitations.add(home.error() != null
                    ? "The home page could not be fetched: " + home.error() + ". Every check that reads it is reported as unknown."
                    : "The home page answered HTTP " + home.status() + " instead of a page. Every check that reads it is reported as unknown.");
            psiFuture.cancel(true);
            robotsFuture.cancel(true);
            return new SiteSnapshot(requestedUrl, pages, fetched, limitations);
        }

        Document homeDoc = home.document();
        Map<PageRole, String> targets = discover(homeDoc, home.finalUrl());
        int budget = properties.maxPages() - 1;
        for (PageRole role : DISCOVERY_ORDER) {
            String target = targets.get(role);
            if (target == null) {
                continue;
            }
            if (HtmlSupport.samePage(target, home.finalUrl())) {
                pages.put(role, home.as(role));
                continue;
            }
            Optional<FetchedPage> already = pages.values().stream()
                    .filter(p -> HtmlSupport.samePage(p.requestedUrl(), target) || HtmlSupport.samePage(p.finalUrl(), target))
                    .findFirst();
            if (already.isPresent()) {
                pages.put(role, already.get().as(role));
                continue;
            }
            if (budget <= 0) {
                limitations.add("The " + role.name().toLowerCase() + " page was found but not fetched: the check reads at most "
                        + properties.maxPages() + " pages.");
                continue;
            }
            if (!clock.instant().isBefore(deadline)) {
                limitations.add("The " + role.name().toLowerCase() + " page was found but not fetched: the site was too slow to read more pages within the time limit.");
                continue;
            }
            URI uri;
            try {
                uri = URI.create(target);
                policy.validate(uri);
            } catch (InvalidUrlException | IllegalArgumentException e) {
                limitations.add("The " + role.name().toLowerCase() + " link points to an address we do not fetch (" + HtmlSupport.shorten(target, 80) + ").");
                continue;
            }
            FetchedPage page = fetcher.fetch(uri, role);
            budget--;
            fetched.add(page);
            pages.put(role, page);
        }

        List<FetchedPage> probes = null;
        if (clock.instant().isBefore(deadline)) {
            probes = probeLinks(homeDoc, pages.values(), deadline);
            fetched.addAll(probes);
        } else {
            limitations.add("Other internal links were not sampled for broken links: the time limit was reached.");
        }

        RobotsResult robots = null;
        SitemapResult sitemap = null;
        try {
            RobotsAndSitemap rs = await(robotsFuture, deadline);
            robots = rs.robots();
            sitemap = rs.sitemap();
            fetched.addAll(rs.fetches());
        } catch (TimeoutException e) {
            robotsFuture.cancel(true);
            limitations.add("robots.txt and the sitemap did not load within the time limit, so those checks are unknown.");
        }

        PsiResult psi = null;
        try {
            psi = await(psiFuture, deadline);
            if (!psi.available()) {
                limitations.add(psi.limitation());
            }
        } catch (TimeoutException e) {
            // Not cancelled on purpose: the result lands in the PSI cache for the host's next run.
            limitations.add("PageSpeed Insights did not answer within the time limit; mobile-friendliness, Core Web Vitals and page weight are unknown. Run the check again in a minute: the result is cached for 24 hours once it arrives.");
        }
        return new SiteSnapshot(requestedUrl, pages, fetched, limitations, robots, sitemap, psi, probes);
    }

    private <T> T await(Future<T> future, Instant deadline) throws TimeoutException {
        long remaining = Math.max(0, Duration.between(clock.instant(), deadline).toMillis());
        try {
            return future.get(remaining, TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            log.warn("Site check side task failed", e.getCause());
            throw new TimeoutException("failed");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TimeoutException("interrupted");
        }
    }

    private RobotsAndSitemap loadRobotsAndSitemap(URI requestedUrl) {
        List<FetchedPage> fetches = new ArrayList<>();
        FetchedPage robotsFetch = fetcher.fetchText(requestedUrl.resolve("/robots.txt"), PageRole.ROBOTS);
        fetches.add(robotsFetch);
        RobotsResult robots = RobotsTxt.parse(robotsFetch);

        String sitemapUrl = robots.sitemapUrls().stream().findFirst().orElse(requestedUrl.resolve("/sitemap.xml").toString());
        URI sitemapUri;
        try {
            sitemapUri = URI.create(sitemapUrl.trim());
            policy.validate(sitemapUri);
        } catch (InvalidUrlException | IllegalArgumentException e) {
            return new RobotsAndSitemap(robots, new SitemapResult(sitemapUrl, 0, false, false, 0, 0, null,
                    "The sitemap address in robots.txt is not one we fetch"), fetches);
        }
        FetchedPage sitemapFetch = fetcher.fetchText(sitemapUri, PageRole.SITEMAP);
        fetches.add(sitemapFetch);
        SitemapResult sitemap = SitemapXml.parse(sitemapFetch);
        if (sitemap.isIndex()) {
            Optional<String> child = SitemapXml.firstChild(sitemapFetch);
            if (child.isPresent()) {
                try {
                    URI childUri = URI.create(child.get());
                    policy.validate(childUri);
                    FetchedPage childFetch = fetcher.fetchText(childUri, PageRole.SITEMAP);
                    fetches.add(childFetch);
                    SitemapResult childResult = SitemapXml.parse(childFetch);
                    sitemap = new SitemapResult(sitemap.url(), sitemap.status(), sitemap.validXml(), true, sitemap.childCount(),
                            childResult.urlCount(), latest(sitemap.latestLastmod(), childResult.latestLastmod()), null);
                } catch (InvalidUrlException | IllegalArgumentException e) {
                    // keep the index-level result
                }
            }
        }
        return new RobotsAndSitemap(robots, sitemap, fetches);
    }

    private static LocalDate latest(LocalDate a, LocalDate b) {
        if (a == null) {
            return b;
        }
        return b == null || a.isAfter(b) ? a : b;
    }

    /** Up to {@code maxLinkProbes} distinct same-site links from the home page that are not pages already fetched. */
    private List<FetchedPage> probeLinks(Document homeDoc, Iterable<FetchedPage> pages, Instant deadline) {
        Set<String> seen = new LinkedHashSet<>();
        for (FetchedPage p : pages) {
            seen.add(HtmlSupport.canonicalForm(p.requestedUrl()));
            seen.add(HtmlSupport.canonicalForm(p.finalUrl()));
        }
        List<FetchedPage> probes = new ArrayList<>();
        for (PageLinks.Link link : PageLinks.internalLinks(homeDoc)) {
            if (probes.size() >= properties.maxLinkProbes() || !clock.instant().isBefore(deadline)) {
                break;
            }
            if (!seen.add(HtmlSupport.canonicalForm(link.url()))) {
                continue;
            }
            URI uri;
            try {
                uri = URI.create(link.url());
                policy.validate(uri);
            } catch (InvalidUrlException | IllegalArgumentException e) {
                continue;
            }
            probes.add(fetcher.probe(uri));
        }
        return probes;
    }

    /** Which URL stands for each role on the home page: links for rooms/contact/about, the CTA for booking. */
    static Map<PageRole, String> discover(Document homeDoc, String homeUrl) {
        Map<PageRole, String> targets = new LinkedHashMap<>();
        for (PageRole role : List.of(PageRole.ROOMS, PageRole.CONTACT, PageRole.ABOUT)) {
            PageLinks.find(homeDoc, role).ifPresent(l -> targets.put(role, l.url()));
        }
        Optional<BookingCta.Cta> cta = BookingCta.find(homeDoc);
        if (cta.isPresent() && cta.get().href().startsWith("http")) {
            targets.put(PageRole.BOOKING, cta.get().href());
        } else {
            PageLinks.find(homeDoc, PageRole.BOOKING).ifPresent(l -> targets.put(PageRole.BOOKING, l.url()));
        }
        return targets;
    }
}
