package com.antenapro.hotelcheck.sitecheck.model;

import java.time.LocalDate;

/**
 * The XML sitemap found through robots.txt or at /sitemap.xml.
 *
 * @param isIndex       the file is a sitemapindex; {@code urlCount} then counts the first child sitemap's URLs
 * @param childCount    number of child sitemaps when {@code isIndex}
 * @param latestLastmod newest {@code <lastmod>} seen, null when none
 */
public record SitemapResult(String url, int status, boolean validXml, boolean isIndex, int childCount, int urlCount,
                            LocalDate latestLastmod, String error) {

    public boolean present() {
        return status >= 200 && status < 300;
    }
}
