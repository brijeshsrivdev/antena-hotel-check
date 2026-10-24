package com.antenapro.hotelcheck.sitecheck.check;

import com.antenapro.hotelcheck.sitecheck.html.HtmlSupport;
import com.antenapro.hotelcheck.sitecheck.html.JsonLd;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Optional;

/** What the site calls the property: JSON-LD name, else {@code og:site_name}, else the first H1, else the title before a separator. */
final class PropertyName {

    private PropertyName() {
    }

    static Optional<String> of(Document home) {
        Optional<String> ld = JsonLd.lodging(home).flatMap(n -> JsonLd.string(n, "name"));
        if (ld.isPresent()) {
            return ld;
        }
        String og = home.select("meta[property=og:site_name]").attr("content").trim();
        if (!og.isEmpty()) {
            return Optional.of(og);
        }
        Element h1 = home.selectFirst("h1");
        if (h1 != null && !HtmlSupport.text(h1).isEmpty()) {
            return Optional.of(HtmlSupport.text(h1));
        }
        String title = home.title().trim();
        if (!title.isEmpty()) {
            return Optional.of(title.split("\\s*[|\\-–—:]\\s*")[0].trim());
        }
        return Optional.empty();
    }

    /** The town from JSON-LD {@code address.addressLocality}, when the site states one. */
    static Optional<String> town(Document home) {
        return JsonLd.lodging(home)
                .map(n -> n.get("address"))
                .filter(a -> a != null && a.isObject())
                .flatMap(a -> JsonLd.string(a, "addressLocality"));
    }
}
