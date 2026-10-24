package com.antenapro.hotelcheck.sitecheck.model;

import org.jsoup.nodes.Document;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Everything the checks are allowed to look at: the pages fetched for one run (never more than five), the
 * robots.txt, sitemap and PageSpeed Insights signals, the sampled internal-link probes, and the limitations
 * met on the way. Nothing here is persisted; the run keeps findings, not pages.
 *
 * @param requestedUrl the normalised URL the visitor typed
 * @param pages        fetched pages by role; the home page is always present, possibly failed
 * @param fetched      every fetch in order, including failures, robots/sitemap and link probes
 * @param limitations  plain sentences shown verbatim in the report
 * @param robots       null when robots.txt was not read (home page failed or deadline)
 * @param sitemap      null when no sitemap was read
 * @param psi          null when PageSpeed Insights was not consulted at all
 * @param linkProbes   sampled internal links from the home page with their status; null when sampling was skipped
 */
public record SiteSnapshot(URI requestedUrl, Map<PageRole, FetchedPage> pages, List<FetchedPage> fetched,
                           List<String> limitations, RobotsResult robots, SitemapResult sitemap, PsiResult psi,
                           List<FetchedPage> linkProbes) {

    /** Pages only — what a run that could not read the home page produces. */
    public SiteSnapshot(URI requestedUrl, Map<PageRole, FetchedPage> pages, List<FetchedPage> fetched, List<String> limitations) {
        this(requestedUrl, pages, fetched, limitations, null, null, null, null);
    }

    public FetchedPage home() {
        return pages.get(PageRole.HOME);
    }

    public Optional<FetchedPage> page(PageRole role) {
        return Optional.ofNullable(pages.get(role));
    }

    public Optional<Document> homeDoc() {
        return home() == null ? Optional.empty() : home().doc();
    }

    public Optional<RobotsResult> robotsResult() {
        return Optional.ofNullable(robots);
    }

    public Optional<SitemapResult> sitemapResult() {
        return Optional.ofNullable(sitemap);
    }

    public Optional<PsiResult> psiResult() {
        return Optional.ofNullable(psi);
    }

    /** Pages that returned HTML, home first. */
    public List<FetchedPage> htmlPages() {
        return pages.values().stream().filter(FetchedPage::ok).toList();
    }

    /** Host of the final home URL (after redirects), lower-case, without a leading {@code www.}. */
    public String host() {
        String url = home() != null && home().finalUrl() != null ? home().finalUrl() : requestedUrl.toString();
        return hostOf(url);
    }

    public static String hostOf(String url) {
        try {
            String h = URI.create(url.trim()).getHost();
            if (h == null) {
                return "";
            }
            h = h.toLowerCase();
            return h.startsWith("www.") ? h.substring(4) : h;
        } catch (IllegalArgumentException e) {
            return "";
        }
    }
}
