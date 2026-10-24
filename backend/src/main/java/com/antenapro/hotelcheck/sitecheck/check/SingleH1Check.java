package com.antenapro.hotelcheck.sitecheck.check;

import com.antenapro.hotelcheck.sitecheck.html.HtmlSupport;
import com.antenapro.hotelcheck.sitecheck.model.Finding;
import com.antenapro.hotelcheck.sitecheck.model.SiteSnapshot;
import com.antenapro.hotelcheck.sitecheck.model.Stage;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.Optional;
import java.util.regex.Pattern;

/** PASS: exactly one H1 on the home page, and it names the property or what it offers. */
class SingleH1Check extends AbstractSiteCheck {

    private static final Pattern OFFER = Pattern.compile("(?i)\\b(hotel|resort|stay|rooms?|villa|cottage|homestay|guest ?house|lodge|inn|suite|beach|retreat)\\b");

    SingleH1Check() {
        super("single-h1", "One H1 heading", Stage.UNDERSTAND,
                "The H1 tells Google what the page is about; several or none dilute that signal.", false);
    }

    @Override
    protected Finding check(SiteSnapshot snapshot, Document home) {
        Elements h1s = home.select("h1");
        if (h1s.isEmpty()) {
            return fail("No <h1> on the home page.", "Add one <h1> naming the hotel and its offer, e.g. \"Sea Breeze Resort – beach stays in Diveagar\".");
        }
        String first = HtmlSupport.text(h1s.first());
        Optional<String> name = PropertyName.of(home);
        boolean names = (name.isPresent() && HtmlSupport.similarity(first, name.get()) >= 0.8) || OFFER.matcher(first).find();
        if (h1s.size() > 1) {
            return partial(h1s.size() + " <h1> tags: " + h1s.stream().map(HtmlSupport::text).map(t -> "\"" + HtmlSupport.shorten(t, 50) + "\"").limit(3).toList(),
                    "Keep one <h1> that names the property; turn the others into <h2>.");
        }
        if (!names) {
            return partial("One <h1>: \"" + HtmlSupport.shorten(first, 100) + "\" — it names neither the hotel nor what it offers.",
                    "Rewrite the <h1> so it names the hotel or its offer, e.g. \"Sea Breeze Resort – beach stays in Diveagar\".");
        }
        return pass("One <h1>: \"" + HtmlSupport.shorten(first, 100) + "\".", "Nothing to do.");
    }
}
