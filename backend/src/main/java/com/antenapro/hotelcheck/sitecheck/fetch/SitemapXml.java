package com.antenapro.hotelcheck.sitecheck.fetch;

import com.antenapro.hotelcheck.sitecheck.model.FetchedPage;
import com.antenapro.hotelcheck.sitecheck.model.SitemapResult;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;

/** Reads a urlset or sitemapindex with jsoup's XML parser; counts URLs and finds the newest lastmod. */
public final class SitemapXml {

    private SitemapXml() {
    }

    public static SitemapResult parse(FetchedPage fetch) {
        if (!fetch.responded()) {
            return new SitemapResult(fetch.requestedUrl(), 0, false, false, 0, 0, null, fetch.error());
        }
        if (fetch.status() < 200 || fetch.status() >= 300 || fetch.text() == null) {
            return new SitemapResult(fetch.finalUrl(), fetch.status(), false, false, 0, 0, null, null);
        }
        Document doc = Jsoup.parse(fetch.text(), fetch.finalUrl(), Parser.xmlParser());
        Element root = doc.children().stream().filter(e -> !e.tagName().startsWith("?") && !e.tagName().startsWith("!")).findFirst().orElse(null);
        if (root == null) {
            return new SitemapResult(fetch.finalUrl(), fetch.status(), false, false, 0, 0, null, null);
        }
        String tag = root.tagName().toLowerCase();
        LocalDate latest = doc.select("lastmod").stream().map(e -> parseDate(e.text())).flatMap(Optional::stream)
                .max(LocalDate::compareTo).orElse(null);
        if (tag.endsWith("sitemapindex")) {
            return new SitemapResult(fetch.finalUrl(), fetch.status(), true, true, root.select("> sitemap > loc").size(), 0, latest, null);
        }
        if (tag.endsWith("urlset")) {
            return new SitemapResult(fetch.finalUrl(), fetch.status(), true, false, 0, root.select("> url > loc").size(), latest, null);
        }
        return new SitemapResult(fetch.finalUrl(), fetch.status(), false, false, 0, 0, null, null);
    }

    /** First child sitemap of an index, when there is one. */
    public static Optional<String> firstChild(FetchedPage fetch) {
        if (fetch.text() == null) {
            return Optional.empty();
        }
        Document doc = Jsoup.parse(fetch.text(), fetch.finalUrl(), Parser.xmlParser());
        Element loc = doc.selectFirst("sitemap > loc");
        return loc == null || loc.text().isBlank() ? Optional.empty() : Optional.of(loc.text().trim());
    }

    static Optional<LocalDate> parseDate(String s) {
        if (s == null || s.isBlank()) {
            return Optional.empty();
        }
        String t = s.trim();
        try {
            return Optional.of(LocalDate.parse(t.length() >= 10 ? t.substring(0, 10) : t));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }
}
