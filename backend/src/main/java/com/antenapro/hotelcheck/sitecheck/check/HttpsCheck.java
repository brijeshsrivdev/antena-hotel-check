package com.antenapro.hotelcheck.sitecheck.check;

import com.antenapro.hotelcheck.sitecheck.model.Finding;
import com.antenapro.hotelcheck.sitecheck.model.SiteSnapshot;
import com.antenapro.hotelcheck.sitecheck.model.Stage;
import org.jsoup.nodes.Document;

import java.util.List;
import java.util.Locale;

/** PASS when the final home URL is https and nothing on the page loads over plain http. */
class HttpsCheck extends AbstractSiteCheck {

    HttpsCheck() {
        super("https", "HTTPS", Stage.DISCOVER,
                "HTTPS is a ranking signal and Chrome marks http:// sites \"Not secure\", which costs clicks.", true);
    }

    @Override
    protected Finding check(SiteSnapshot snapshot, Document home) {
        String finalUrl = snapshot.home().finalUrl();
        if (!finalUrl.toLowerCase(Locale.ROOT).startsWith("https://")) {
            return fail("The home page is served at " + finalUrl + " (not https).",
                    "Install an SSL certificate and redirect every http:// address to https://.");
        }
        List<String> mixed = home.select("img[src^=http:], script[src^=http:], link[rel=stylesheet][href^=http:], iframe[src^=http:], source[src^=http:], video[src^=http:]")
                .stream().map(e -> e.hasAttr("src") ? e.attr("src") : e.attr("href")).distinct().limit(3).toList();
        if (!mixed.isEmpty()) {
            return partial("The page is https but loads resources over http: " + String.join(", ", mixed),
                    "Change these resource addresses to https:// so browsers stop warning about mixed content.");
        }
        return pass("Served at " + finalUrl + " with no http:// resources on the page.", "Nothing to do.");
    }
}
