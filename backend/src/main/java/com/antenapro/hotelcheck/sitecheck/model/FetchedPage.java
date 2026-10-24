package com.antenapro.hotelcheck.sitecheck.model;

import org.jsoup.nodes.Document;

import java.util.Map;
import java.util.Optional;

/**
 * One HTTP fetch. {@code document} is present only for a 2xx HTML response; {@code text} only for a non-HTML
 * text response read as text (robots.txt, a sitemap); {@code error} explains a fetch that produced no response
 * (timeout, refused connection, policy rejection on a redirect).
 *
 * @param headers response headers the checks read, keys lower-cased (content-type, x-robots-tag, last-modified)
 */
public record FetchedPage(PageRole role, String requestedUrl, String finalUrl, int status, long ms, long bytes,
                          boolean truncated, Map<String, String> headers, Document document, String text, String error) {

    public static FetchedPage failed(PageRole role, String requestedUrl, long ms, String error) {
        return new FetchedPage(role, requestedUrl, requestedUrl, 0, ms, 0, false, Map.of(), null, null, error);
    }

    public boolean ok() {
        return status >= 200 && status < 300 && document != null;
    }

    public boolean responded() {
        return status > 0;
    }

    /** "HTTP 404", "Connection refused…" — for evidence lines. */
    public String outcome() {
        return error != null ? error : "HTTP " + status;
    }

    public Optional<Document> doc() {
        return Optional.ofNullable(document);
    }

    public Optional<String> header(String name) {
        return Optional.ofNullable(headers.get(name.toLowerCase()));
    }

    public FetchedPage as(PageRole newRole) {
        return new FetchedPage(newRole, requestedUrl, finalUrl, status, ms, bytes, truncated, headers, document, text, error);
    }
}
