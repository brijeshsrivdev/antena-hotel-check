package com.antenapro.hotelcheck.sitecheck.check;

import com.antenapro.hotelcheck.sitecheck.html.HtmlSupport;
import com.antenapro.hotelcheck.sitecheck.model.FetchedPage;
import com.antenapro.hotelcheck.sitecheck.model.Finding;
import com.antenapro.hotelcheck.sitecheck.model.SiteSnapshot;
import com.antenapro.hotelcheck.sitecheck.model.Stage;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** PASS: 70–160 characters, not cut off with an ellipsis, different on every fetched page. */
class MetaDescriptionCheck extends AbstractSiteCheck {

    MetaDescriptionCheck() {
        super("meta-description", "Meta description", Stage.DISCOVER,
                "Google often shows the description under the title; a good one lifts the click-through rate from results.", false);
    }

    @Override
    protected Finding check(SiteSnapshot snapshot, Document home) {
        String homeDesc = description(home);
        if (homeDesc.isEmpty()) {
            return fail("The home page has no <meta name=\"description\">.",
                    "Add a 70–160 character description that says what the hotel offers, where it is and how to book.");
        }
        List<String> problems = new ArrayList<>();
        List<String> seen = new ArrayList<>();
        StringBuilder evidence = new StringBuilder();
        for (FetchedPage page : snapshot.htmlPages()) {
            String d = description(page.document());
            String role = page.role().name().toLowerCase(Locale.ROOT);
            if (d.isEmpty()) {
                evidence.append(role).append(": none; ");
                if (page.role() != com.antenapro.hotelcheck.sitecheck.model.PageRole.HOME) {
                    problems.add(role + " page has no description");
                }
                continue;
            }
            evidence.append(role).append(": \"").append(HtmlSupport.shorten(d, 60)).append("\" (").append(d.length()).append(" chars); ");
            if (d.length() < 70 || d.length() > 160) {
                problems.add(role + " description is " + d.length() + " characters");
            }
            if (d.endsWith("…") || d.endsWith("...")) {
                problems.add(role + " description is cut off");
            }
            if (seen.contains(d)) {
                problems.add(role + " description repeats another page's");
            }
            seen.add(d);
        }
        if (problems.isEmpty()) {
            return pass(evidence.toString().trim(), "Nothing to do.");
        }
        return partial(evidence + "Problems: " + String.join("; ", problems),
                "Write a distinct 70–160 character description for each page, ending in a full sentence.");
    }

    private static String description(Document doc) {
        return doc.select("meta[name=description]").attr("content").replaceAll("\\s+", " ").trim();
    }
}
