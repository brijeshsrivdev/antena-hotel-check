package com.antenapro.hotelcheck.sitecheck.check;

import com.antenapro.hotelcheck.sitecheck.html.HtmlSupport;
import com.antenapro.hotelcheck.sitecheck.model.FetchedPage;
import com.antenapro.hotelcheck.sitecheck.model.Finding;
import com.antenapro.hotelcheck.sitecheck.model.PageRole;
import com.antenapro.hotelcheck.sitecheck.model.SiteSnapshot;
import com.antenapro.hotelcheck.sitecheck.model.Stage;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** PASS: every fetched page carries a canonical link pointing at itself. */
class CanonicalCheck extends AbstractSiteCheck {

    CanonicalCheck() {
        super("canonical", "Canonical URL", Stage.DISCOVER,
                "A self-referencing canonical stops www/non-www, http/https and tracking-parameter copies from splitting the page's ranking.", true);
    }

    @Override
    protected Finding check(SiteSnapshot snapshot, Document home) {
        Element homeCanonical = home.selectFirst("link[rel=canonical]");
        if (homeCanonical == null || homeCanonical.absUrl("href").isEmpty()) {
            return fail("The home page has no <link rel=\"canonical\">.",
                    "Add <link rel=\"canonical\" href=\"" + snapshot.home().finalUrl() + "\"> to the home page and the matching tag to every other page.");
        }
        List<String> problems = new ArrayList<>();
        StringBuilder evidence = new StringBuilder();
        for (FetchedPage page : snapshot.htmlPages()) {
            Element c = page.document().selectFirst("link[rel=canonical]");
            String role = page.role().name().toLowerCase(Locale.ROOT);
            if (c == null || c.absUrl("href").isEmpty()) {
                evidence.append(role).append(": none; ");
                problems.add(role + " page has no canonical");
                continue;
            }
            String href = c.absUrl("href");
            evidence.append(role).append(": ").append(HtmlSupport.shorten(href, 60)).append("; ");
            if (!HtmlSupport.samePage(href, page.finalUrl())) {
                problems.add(role + " canonical points to " + HtmlSupport.shorten(href, 60) + " instead of " + HtmlSupport.shorten(page.finalUrl(), 60));
            }
        }
        if (problems.isEmpty()) {
            return pass(evidence.toString().trim(), "Nothing to do.");
        }
        boolean homeWrong = problems.stream().anyMatch(p -> p.startsWith(PageRole.HOME.name().toLowerCase(Locale.ROOT)));
        String fix = "Make each page's canonical link point at that page's own https address.";
        return homeWrong ? fail(evidence + "Problems: " + String.join("; ", problems), fix)
                : partial(evidence + "Problems: " + String.join("; ", problems), fix);
    }
}
