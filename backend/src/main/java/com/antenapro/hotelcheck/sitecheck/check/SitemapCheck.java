package com.antenapro.hotelcheck.sitecheck.check;

import com.antenapro.hotelcheck.sitecheck.model.Finding;
import com.antenapro.hotelcheck.sitecheck.model.SiteSnapshot;
import com.antenapro.hotelcheck.sitecheck.model.SitemapResult;
import com.antenapro.hotelcheck.sitecheck.model.Stage;
import org.jsoup.nodes.Document;

import java.util.Optional;

/** PASS: a sitemap is referenced in robots.txt or served at /sitemap.xml, is valid XML and lists at least one URL. */
class SitemapCheck extends AbstractSiteCheck {

    SitemapCheck() {
        super("sitemap", "XML sitemap", Stage.DISCOVER,
                "The sitemap is how Google discovers room and offer pages that are not linked prominently, and when they changed.", true);
    }

    @Override
    protected Finding check(SiteSnapshot snapshot, Document home) {
        Optional<SitemapResult> sitemap = snapshot.sitemapResult();
        if (sitemap.isEmpty()) {
            return unknown("The sitemap was not read within the time limit.", "Run the check again.");
        }
        SitemapResult s = sitemap.get();
        if (s.status() == 0) {
            return s.error() != null && s.error().contains("not one we fetch")
                    ? fail("robots.txt points to a sitemap at " + s.url() + ", which is not a public address.", "Point the Sitemap: line at the site's own https address.")
                    : unknown(s.url() + " could not be fetched: " + s.error() + ".", "Run the check again.");
        }
        if (!s.present()) {
            return fail("No sitemap: " + s.url() + " answered HTTP " + s.status() + " and robots.txt names none.",
                    "Publish an XML sitemap at /sitemap.xml listing every page with its lastmod, and reference it from robots.txt.");
        }
        if (!s.validXml()) {
            return fail(s.url() + " answered HTTP " + s.status() + " but is not a valid urlset or sitemapindex.",
                    "Serve a valid XML sitemap (a <urlset> of <url><loc> entries) at that address.");
        }
        int urls = s.urlCount();
        String where = s.isIndex() ? s.url() + " is a sitemap index with " + s.childCount() + " sitemap(s); the first lists " + urls + " URL(s)"
                : s.url() + " lists " + urls + " URL(s)";
        String lastmod = s.latestLastmod() == null ? ", no lastmod" : ", newest lastmod " + s.latestLastmod();
        if (urls < 1) {
            return fail(where + lastmod + ".", "Add every public page (home, rooms, contact, booking) to the sitemap.");
        }
        return pass(where + lastmod + ".", s.latestLastmod() == null ? "Add <lastmod> dates so Google knows which pages changed." : "Nothing to do.");
    }
}
