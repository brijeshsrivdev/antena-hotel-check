package com.antenapro.hotelcheck.sitecheck.check;

import com.antenapro.hotelcheck.sitecheck.html.HtmlSupport;
import com.antenapro.hotelcheck.sitecheck.html.JsonLd;
import com.antenapro.hotelcheck.sitecheck.model.Finding;
import com.antenapro.hotelcheck.sitecheck.model.SiteSnapshot;
import com.antenapro.hotelcheck.sitecheck.model.Stage;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** PASS: title, H1, JSON-LD name and footer agree on the property name (similarity ≥ 0.8). */
class HotelNameConsistencyCheck extends AbstractSiteCheck {

    HotelNameConsistencyCheck() {
        super("hotel-name-consistency", "Hotel name consistency", Stage.UNDERSTAND,
                "Google matches the name on the site against the Business Profile and directories; one spelling everywhere strengthens local ranking.", false);
    }

    @Override
    protected Finding check(SiteSnapshot snapshot, Document home) {
        Map<String, String> sources = new LinkedHashMap<>();
        String title = home.title().trim();
        if (!title.isEmpty()) {
            sources.put("title", title);
        }
        Element h1 = home.selectFirst("h1");
        if (h1 != null && !HtmlSupport.text(h1).isEmpty()) {
            sources.put("h1", HtmlSupport.text(h1));
        }
        JsonLd.lodging(home).flatMap(n -> JsonLd.string(n, "name")).ifPresent(n -> sources.put("json-ld", n));
        String reference = sources.getOrDefault("json-ld", sources.getOrDefault("h1", sources.getOrDefault("title", "")));
        Element footer = home.selectFirst("footer, #footer, [class~=(?i)footer]");
        if (footer != null && !reference.isEmpty()) {
            String footerText = HtmlSupport.text(footer);
            sources.put("footer", HtmlSupport.normalizeName(footerText).contains(HtmlSupport.normalizeName(reference)) ? reference : HtmlSupport.shorten(footerText, 60));
        }
        if (sources.size() < 2) {
            return fail("The property name appears in " + sources.size() + " place(s) only: " + sources,
                    "State the hotel's name in the title, the H1, the footer and the structured data, spelled the same way.");
        }
        List<String> disagree = new ArrayList<>();
        StringBuilder evidence = new StringBuilder();
        for (Map.Entry<String, String> e : sources.entrySet()) {
            double s = HtmlSupport.similarity(e.getValue(), reference);
            evidence.append(e.getKey()).append(": \"").append(HtmlSupport.shorten(e.getValue(), 50)).append("\"; ");
            if (s < 0.8) {
                disagree.add(e.getKey());
            }
        }
        if (disagree.isEmpty()) {
            return sources.size() >= 3 ? pass(evidence.toString().trim(), "Nothing to do.")
                    : partial(evidence + "Only " + sources.size() + " places name the property.",
                    "Add the hotel's name to the footer and the structured data as well, spelled the same way.");
        }
        String fix = "Use one spelling of the hotel's name in the title, H1, footer and structured data (" + String.join(", ", disagree) + " differ).";
        return disagree.size() >= 2 ? fail(evidence.toString().trim(), fix) : partial(evidence.toString().trim(), fix);
    }
}
