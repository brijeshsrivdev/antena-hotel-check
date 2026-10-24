package com.antenapro.hotelcheck.sitecheck.fetch;

import com.antenapro.hotelcheck.sitecheck.SiteCheckProperties;
import com.antenapro.hotelcheck.sitecheck.html.BookingCta;
import com.antenapro.hotelcheck.sitecheck.html.HtmlSupport;
import com.antenapro.hotelcheck.sitecheck.html.PageLinks;
import com.antenapro.hotelcheck.sitecheck.model.FetchedPage;
import com.antenapro.hotelcheck.sitecheck.model.PageRole;
import com.antenapro.hotelcheck.sitecheck.model.SiteSnapshot;
import org.jsoup.nodes.Document;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Fetches the home page, then up to four more pages discovered on it (rooms, contact, the booking CTA's
 * target, about), in that order, stopping at the page budget or the fetch deadline. A discovered link that
 * is the home page again is not refetched; one that the {@link UrlPolicy} rejects is recorded as a
 * limitation.
 */
public class SiteSnapshotLoader {

    private static final List<PageRole> DISCOVERY_ORDER = List.of(PageRole.ROOMS, PageRole.CONTACT, PageRole.BOOKING, PageRole.ABOUT);

    private final SiteCheckProperties properties;
    private final PageFetcher fetcher;
    private final UrlPolicy policy;
    private final Clock clock;

    public SiteSnapshotLoader(SiteCheckProperties properties, PageFetcher fetcher, UrlPolicy policy, Clock clock) {
        this.properties = properties;
        this.fetcher = fetcher;
        this.policy = policy;
        this.clock = clock;
    }

    /**
     * @param deadline no new fetch starts after this instant; pages left unfetched become a limitation
     */
    public SiteSnapshot load(URI requestedUrl, Instant deadline) {
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
        return new SiteSnapshot(requestedUrl, pages, fetched, limitations);
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
