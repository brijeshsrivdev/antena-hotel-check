package com.antenapro.hotelcheck.sitecheck.model;

import java.util.List;

/**
 * The site's robots.txt as Googlebot would read it.
 *
 * @param status      HTTP status, 0 when the fetch failed
 * @param blocksAll   a {@code Disallow: /} applies to {@code *} or to Googlebot
 * @param blockedBy   the offending record's user-agent line, for evidence
 * @param sitemapUrls every {@code Sitemap:} line
 */
public record RobotsResult(String url, int status, boolean blocksAll, String blockedBy, List<String> sitemapUrls, String error) {

    public boolean present() {
        return status >= 200 && status < 300;
    }
}
