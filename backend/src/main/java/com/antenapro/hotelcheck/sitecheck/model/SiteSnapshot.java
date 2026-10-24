package com.antenapro.hotelcheck.sitecheck.model;

import org.jsoup.nodes.Document;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Everything the checks are allowed to look at: the pages fetched for one run (never more than five) and the
 * limitations met while fetching them. Nothing here is persisted; the run keeps findings, not pages.
 *
 * @param requestedUrl the normalised URL the visitor typed
 * @param pages        fetched pages by role; the home page is always present, possibly failed
 * @param fetched      every fetch in order, including failures and pages that turned out to be the home page
 * @param limitations  plain sentences shown verbatim in the report
 */
public record SiteSnapshot(URI requestedUrl, Map<PageRole, FetchedPage> pages, List<FetchedPage> fetched,
                           List<String> limitations) {

    public FetchedPage home() {
        return pages.get(PageRole.HOME);
    }

    public Optional<FetchedPage> page(PageRole role) {
        return Optional.ofNullable(pages.get(role));
    }

    public Optional<Document> homeDoc() {
        return home() == null ? Optional.empty() : home().doc();
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
