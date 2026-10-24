package com.antenapro.hotelcheck.sitecheck.check;

import com.antenapro.hotelcheck.sitecheck.html.HtmlSupport;
import com.antenapro.hotelcheck.sitecheck.html.PageLinks;
import com.antenapro.hotelcheck.sitecheck.model.FetchedPage;
import com.antenapro.hotelcheck.sitecheck.model.Finding;
import com.antenapro.hotelcheck.sitecheck.model.PageRole;
import com.antenapro.hotelcheck.sitecheck.model.SiteSnapshot;
import com.antenapro.hotelcheck.sitecheck.model.Stage;
import org.jsoup.nodes.Document;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/** PASS: a rooms/accommodation page exists with at least two room types and a price or "from ₹". */
class RoomsPageCheck extends AbstractSiteCheck {

    private static final Pattern ROOM_HEADING = Pattern.compile("(?i)\\b(rooms?|suites?|villas?|cottages?|deluxe|standard|executive|premium|dormitory|tents?|family|ac|non-ac|studio|penthouse)\\b");
    private static final Pattern PRICE = Pattern.compile("(?i)(₹|rs\\.?|inr)\\s?\\d[\\d,]*|\\bfrom\\s+(₹|rs\\.?|inr)|\\b\\d[\\d,]*\\s?/\\s?(night|day)\\b");

    RoomsPageCheck() {
        super("rooms-page", "Rooms page", Stage.UNDERSTAND,
                "Guests search for room types and prices; a page listing them is what Google matches lodging queries to.", false);
    }

    @Override
    protected Finding check(SiteSnapshot snapshot, Document home) {
        Optional<FetchedPage> rooms = snapshot.page(PageRole.ROOMS);
        if (rooms.isEmpty()) {
            Set<String> onHome = roomTypes(home);
            if (onHome.size() >= 2) {
                return partial("No rooms page linked from the home page; the home page itself lists " + onHome.size() + " room types: " + HtmlSupport.shorten(String.join(", ", onHome), 120),
                        "Give rooms their own page (e.g. /rooms) with a photo, description and price for each room type, and link it from the menu.");
            }
            boolean linkFoundButNotFetched = PageLinks.find(home, PageRole.ROOMS).isPresent();
            if (linkFoundButNotFetched) {
                return unknown("A rooms link exists but the page was not fetched within the run's page or time budget.", "Run the check again.");
            }
            return fail("No rooms or accommodation page is linked from the home page and the home page lists " + onHome.size() + " room type(s).",
                    "Add a rooms page listing every room type with a photo, description and price, and link it from the menu.");
        }
        FetchedPage page = rooms.get();
        if (!page.ok()) {
            return fail("The rooms link " + HtmlSupport.shorten(page.requestedUrl(), 80) + " answered "
                    + (page.error() != null ? page.error() : "HTTP " + page.status()) + ".",
                    "Fix the rooms page so it loads, then list every room type with a price.");
        }
        Set<String> types = roomTypes(page.document());
        boolean price = PRICE.matcher(HtmlSupport.visibleText(page.document())).find();
        String where = page.role() == PageRole.ROOMS && HtmlSupport.samePage(page.finalUrl(), snapshot.home().finalUrl()) ? "home page (rooms section)" : HtmlSupport.shorten(page.finalUrl(), 80);
        String evidence = where + ": " + types.size() + " room type heading(s) [" + HtmlSupport.shorten(String.join(", ", types), 120) + "], price " + (price ? "shown" : "not shown") + ".";
        if (types.size() >= 2 && price) {
            return pass(evidence, "Nothing to do.");
        }
        if (types.isEmpty()) {
            return fail(evidence, "List each room type under its own heading with a photo, description and a price or \"from ₹…\".");
        }
        return partial(evidence, types.size() < 2 ? "List every room type under its own heading, with a price or \"from ₹…\" for each."
                : "Show a price or \"from ₹…\" for each room type; guests and Google both look for it.");
    }

    private static Set<String> roomTypes(Document doc) {
        Set<String> types = new LinkedHashSet<>();
        doc.select("h2, h3, h4").forEach(h -> {
            String t = HtmlSupport.text(h);
            if (!t.isEmpty() && t.length() <= 60 && ROOM_HEADING.matcher(t).find() && !t.toLowerCase().startsWith("our rooms") && !t.equalsIgnoreCase("rooms")) {
                types.add(t);
            }
        });
        return types;
    }
}
