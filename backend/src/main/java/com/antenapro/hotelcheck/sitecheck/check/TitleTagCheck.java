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
import java.util.Optional;

/** PASS: 30–60 characters, names the hotel and its town, different on every fetched page. */
class TitleTagCheck extends AbstractSiteCheck {

    TitleTagCheck() {
        super("title", "Title tag", Stage.DISCOVER,
                "The title is the strongest on-page relevance signal and the headline Google shows in results.", false);
    }

    @Override
    protected Finding check(SiteSnapshot snapshot, Document home) {
        String homeTitle = home.title().trim();
        if (homeTitle.isEmpty()) {
            return fail("The home page has no <title>.",
                    "Add a 30–60 character title naming the hotel and its town, e.g. \"Sea Breeze Resort – Beach Hotel in Diveagar\".");
        }
        List<String> problems = new ArrayList<>();
        List<String> seen = new ArrayList<>();
        StringBuilder evidence = new StringBuilder();
        for (FetchedPage page : snapshot.htmlPages()) {
            String t = page.document().title().trim();
            String role = page.role().name().toLowerCase(Locale.ROOT);
            evidence.append(role).append(": \"").append(HtmlSupport.shorten(t, 70)).append("\" (").append(t.length()).append(" chars); ");
            if (t.length() < 30 || t.length() > 60) {
                problems.add(role + " title is " + t.length() + " characters");
            }
            if (seen.contains(t)) {
                problems.add(role + " title repeats another page's title");
            }
            seen.add(t);
        }
        Optional<String> name = PropertyName.of(home);
        Optional<String> town = PropertyName.town(home);
        if (name.isPresent() && HtmlSupport.similarity(homeTitle, name.get()) < 0.8
                && !HtmlSupport.normalizeName(homeTitle).contains(HtmlSupport.normalizeName(name.get()))) {
            problems.add("home title does not name the hotel (\"" + HtmlSupport.shorten(name.get(), 40) + "\")");
        }
        if (town.isPresent() && !HtmlSupport.normalizeName(homeTitle).contains(HtmlSupport.normalizeName(town.get()))) {
            problems.add("home title does not name the town (" + town.get() + ")");
        }
        if (problems.isEmpty()) {
            return pass(evidence.toString().trim(), "Nothing to do.");
        }
        boolean duplicate = problems.stream().anyMatch(p -> p.contains("repeats"));
        String fix = "Give each page its own 30–60 character title that names the hotel and its town.";
        return duplicate ? fail(evidence + "Problems: " + String.join("; ", problems), fix)
                : partial(evidence + "Problems: " + String.join("; ", problems), fix);
    }
}
