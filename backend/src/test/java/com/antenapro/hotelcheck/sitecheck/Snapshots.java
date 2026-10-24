package com.antenapro.hotelcheck.sitecheck;

import com.antenapro.hotelcheck.sitecheck.fetch.PageFetcher;
import com.antenapro.hotelcheck.sitecheck.html.HtmlSupport;
import com.antenapro.hotelcheck.sitecheck.model.FetchedPage;
import com.antenapro.hotelcheck.sitecheck.model.PageRole;
import com.antenapro.hotelcheck.sitecheck.model.PsiResult;
import com.antenapro.hotelcheck.sitecheck.model.RobotsResult;
import com.antenapro.hotelcheck.sitecheck.model.SiteSnapshot;
import com.antenapro.hotelcheck.sitecheck.model.SitemapResult;
import com.antenapro.hotelcheck.sitecheck.psi.PsiClient;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Builds snapshots from HTML strings and classpath fixtures; no network anywhere in the tests. */
public final class Snapshots {

    private Snapshots() {
    }

    public static String fixture(String name) {
        try (InputStream in = Snapshots.class.getResourceAsStream("/sitecheck/" + name)) {
            if (in == null) {
                throw new IllegalArgumentException("No fixture " + name);
            }
            int year = HtmlSupport.currentYear();
            LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
            return new String(in.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("{{YEAR}}", String.valueOf(year))
                    .replace("{{YEAR-1}}", String.valueOf(year - 1))
                    .replace("{{YEAR-5}}", String.valueOf(year - 5))
                    .replace("{{TODAY}}", today.toString())
                    .replace("{{TODAY-7}}", today.minusDays(7).toString())
                    .replace("{{TODAY-400}}", today.minusDays(400).toString())
                    .replace("{{TODAY-420}}", today.minusDays(420).toString());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static FetchedPage page(PageRole role, String url, String html) {
        return page(role, url, 200, html, Map.of());
    }

    public static FetchedPage page(PageRole role, String url, int status, String html, Map<String, String> extraHeaders) {
        Map<String, String> headers = new HashMap<>(Map.of("content-type", "text/html; charset=utf-8"));
        headers.putAll(extraHeaders);
        return new FetchedPage(role, url, url, status, 120, html.length(), false, headers,
                status >= 200 && status < 300 ? Jsoup.parse(html, url) : null, null, null);
    }

    public static FetchedPage text(PageRole role, String url, int status, String body) {
        return new FetchedPage(role, url, url, status, 80, body == null ? 0 : body.length(), false,
                Map.of("content-type", "text/plain"), null, status >= 200 && status < 300 ? body : null, null);
    }

    public static FetchedPage failed(PageRole role, String url) {
        return FetchedPage.failed(role, url, 3000, "Connection refused or timed out");
    }

    /** A snapshot with only a home page: no robots, sitemap, PSI or probes, so those checks report UNKNOWN. */
    public static SiteSnapshot home(String url, String html) {
        return of(page(PageRole.HOME, url, html));
    }

    public static SiteSnapshot of(FetchedPage... pages) {
        return with(null, null, null, null, pages);
    }

    public static SiteSnapshot with(RobotsResult robots, SitemapResult sitemap, PsiResult psi, List<FetchedPage> probes, FetchedPage... pages) {
        Map<PageRole, FetchedPage> byRole = new EnumMap<>(PageRole.class);
        for (FetchedPage p : pages) {
            byRole.put(p.role(), p);
        }
        return new SiteSnapshot(URI.create(pages[0].requestedUrl()), byRole, List.of(pages), List.of(), robots, sitemap, psi, probes);
    }

    public static SiteSnapshot unreadableHome(String url) {
        return of(failed(PageRole.HOME, url));
    }

    /** Returns one fixed result; counts calls. */
    public static final class FakePsiClient implements PsiClient {
        private final PsiResult result;
        public int calls;

        public FakePsiClient(PsiResult result) {
            this.result = result;
        }

        @Override
        public PsiResult analyze(URI url) {
            calls++;
            return result;
        }
    }

    /** Serves pages and text from maps keyed by URL; anything else answers 404, or a connection failure when asked. */
    public static final class FakeFetcher implements PageFetcher {
        private final Map<String, String> html = new HashMap<>();
        private final Map<String, String> texts = new HashMap<>();
        private final Map<String, Integer> statuses = new HashMap<>();
        private boolean failUnknown;
        public final List<String> requested = new ArrayList<>();
        public final List<String> probed = new ArrayList<>();

        public FakeFetcher serve(String url, String body) {
            html.put(HtmlSupport.canonicalForm(url), body);
            return this;
        }

        public FakeFetcher serveText(String url, String body) {
            texts.put(HtmlSupport.canonicalForm(url), body);
            return this;
        }

        public FakeFetcher status(String url, int status) {
            statuses.put(HtmlSupport.canonicalForm(url), status);
            return this;
        }

        public FakeFetcher failUnknown() {
            this.failUnknown = true;
            return this;
        }

        @Override
        public FetchedPage fetch(URI url, PageRole role) {
            requested.add(url.toString());
            String key = HtmlSupport.canonicalForm(url.toString());
            if (statuses.containsKey(key)) {
                return page(role, url.toString(), statuses.get(key), "", Map.of());
            }
            String body = html.get(key);
            if (body == null) {
                return failUnknown ? failed(role, url.toString()) : page(role, url.toString(), 404, "", Map.of());
            }
            return page(role, url.toString(), body);
        }

        @Override
        public FetchedPage fetchText(URI url, PageRole role) {
            requested.add(url.toString());
            String key = HtmlSupport.canonicalForm(url.toString());
            if (statuses.containsKey(key)) {
                return text(role, url.toString(), statuses.get(key), null);
            }
            String body = texts.get(key);
            if (body == null) {
                return failUnknown ? failed(role, url.toString()) : text(role, url.toString(), 404, null);
            }
            return text(role, url.toString(), 200, body);
        }

        @Override
        public FetchedPage probe(URI url) {
            probed.add(url.toString());
            String key = HtmlSupport.canonicalForm(url.toString());
            if (statuses.containsKey(key)) {
                return text(PageRole.LINK, url.toString(), statuses.get(key), null);
            }
            if (html.containsKey(key) || texts.containsKey(key)) {
                return text(PageRole.LINK, url.toString(), 200, null);
            }
            return failUnknown ? failed(PageRole.LINK, url.toString()) : text(PageRole.LINK, url.toString(), 404, null);
        }
    }
}
