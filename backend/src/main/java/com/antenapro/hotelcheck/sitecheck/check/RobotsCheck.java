package com.antenapro.hotelcheck.sitecheck.check;

import com.antenapro.hotelcheck.sitecheck.model.Finding;
import com.antenapro.hotelcheck.sitecheck.model.RobotsResult;
import com.antenapro.hotelcheck.sitecheck.model.SiteSnapshot;
import com.antenapro.hotelcheck.sitecheck.model.Stage;
import org.jsoup.nodes.Document;

import java.util.Optional;

/** PASS: robots.txt is present and does not disallow the whole site for {@code *} or Googlebot. */
class RobotsCheck extends AbstractSiteCheck {

    RobotsCheck() {
        super("robots", "robots.txt", Stage.DISCOVER,
                "A robots.txt that disallows / keeps Googlebot out entirely; nothing else on the site matters then (our own tenants were blocked this way, audit A1).", true);
    }

    @Override
    protected Finding check(SiteSnapshot snapshot, Document home) {
        Optional<RobotsResult> robots = snapshot.robotsResult();
        if (robots.isEmpty()) {
            return unknown("robots.txt was not read within the time limit.", "Run the check again.");
        }
        RobotsResult r = robots.get();
        if (r.status() == 0) {
            return unknown(r.url() + " could not be fetched: " + r.error() + ".", "Run the check again.");
        }
        if (r.status() >= 500) {
            return fail(r.url() + " answered HTTP " + r.status() + "; Google stops crawling a site whose robots.txt errors.",
                    "Make /robots.txt answer 200 (or 404) instead of a server error.");
        }
        if (!r.present()) {
            return partial("No robots.txt (" + r.url() + " answered HTTP " + r.status() + "); Google treats this as allow-all.",
                    "Add a /robots.txt with \"User-agent: *\", \"Allow: /\" and a \"Sitemap:\" line pointing at your XML sitemap.");
        }
        if (r.blocksAll()) {
            return fail(r.url() + " contains \"" + r.blockedBy() + "\", which blocks Google from the whole site.",
                    "Remove the \"Disallow: /\" line (or change it to \"Allow: /\") so Google can crawl the site.");
        }
        return pass(r.url() + " present, does not block Googlebot" + (r.sitemapUrls().isEmpty() ? ", no Sitemap line." : ", lists " + r.sitemapUrls().size() + " sitemap(s)."),
                r.sitemapUrls().isEmpty() ? "Add a \"Sitemap:\" line to robots.txt so crawlers find the sitemap." : "Nothing to do.");
    }
}
