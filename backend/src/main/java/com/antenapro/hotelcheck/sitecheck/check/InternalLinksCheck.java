package com.antenapro.hotelcheck.sitecheck.check;

import com.antenapro.hotelcheck.sitecheck.html.BookingCta;
import com.antenapro.hotelcheck.sitecheck.html.HtmlSupport;
import com.antenapro.hotelcheck.sitecheck.html.PageLinks;
import com.antenapro.hotelcheck.sitecheck.model.Finding;
import com.antenapro.hotelcheck.sitecheck.model.PageRole;
import com.antenapro.hotelcheck.sitecheck.model.SiteSnapshot;
import com.antenapro.hotelcheck.sitecheck.model.Stage;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** PASS: the home page links to rooms, contact and booking with anchor text that says where the link goes. */
class InternalLinksCheck extends AbstractSiteCheck {

    InternalLinksCheck() {
        super("internal-links", "Internal links", Stage.EXPLORE,
                "Googlebot reaches room and contact pages through links from the home page; descriptive anchor text tells it what those pages are.", false);
    }

    @Override
    protected Finding check(SiteSnapshot snapshot, Document home) {
        List<String> found = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        List<String> vague = new ArrayList<>();
        for (PageRole role : List.of(PageRole.ROOMS, PageRole.CONTACT, PageRole.BOOKING)) {
            Optional<PageLinks.Link> link = PageLinks.find(home, role);
            String label = role.name().toLowerCase();
            if (link.isEmpty() && role == PageRole.BOOKING) {
                Optional<BookingCta.Cta> cta = BookingCta.find(home);
                if (cta.isPresent()) {
                    found.add(label + ": " + cta.get().describe());
                    continue;
                }
            }
            if (link.isEmpty()) {
                missing.add(label);
            } else if (!PageLinks.isDescriptive(link.get().text())) {
                vague.add(label + ": " + link.get().describe());
            } else {
                found.add(label + ": " + link.get().describe());
            }
        }
        String evidence = HtmlSupport.shorten(String.join(" | ", found), 200)
                + (missing.isEmpty() ? "" : " Missing: " + String.join(", ", missing) + ".")
                + (vague.isEmpty() ? "" : " Vague anchor text: " + String.join(", ", vague) + ".");
        if (missing.isEmpty() && vague.isEmpty()) {
            return pass(evidence, "Nothing to do.");
        }
        if (found.isEmpty() && vague.isEmpty()) {
            return fail(evidence.trim(), "Link the rooms, contact and booking pages from the home page menu with text such as \"Rooms & Tariff\", \"Contact\", \"Book Now\".");
        }
        return partial(evidence.trim(), "Add the missing links to the home page menu and replace \"click here\"-style anchors with words that name the page.");
    }
}
