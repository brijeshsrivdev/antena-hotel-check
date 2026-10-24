package com.antenapro.hotelcheck.sitecheck.check;

import com.antenapro.hotelcheck.sitecheck.html.BookingCta;
import com.antenapro.hotelcheck.sitecheck.model.Finding;
import com.antenapro.hotelcheck.sitecheck.model.SiteSnapshot;
import com.antenapro.hotelcheck.sitecheck.model.Stage;
import org.jsoup.nodes.Document;

import java.util.Optional;

/** PASS: an element whose text or href says book / reserve / check availability / wa.me, high on the home page. */
class BookingCtaCheck extends AbstractSiteCheck {

    BookingCtaCheck() {
        super("booking-cta", "Booking call to action", Stage.BOOK,
                "Every other signal exists to bring a guest to this button; without it the visit ends on an OTA.", true);
    }

    @Override
    protected Finding check(SiteSnapshot snapshot, Document home) {
        Optional<BookingCta.Cta> cta = BookingCta.find(home);
        if (cta.isEmpty()) {
            return fail("No link or button on the home page says book, reserve or check availability, and none opens WhatsApp.",
                    "Add a \"Book Now\" button in the header that opens your booking page or WhatsApp.");
        }
        BookingCta.Cta c = cta.get();
        String evidence = c.describe() + (c.aboveFold() ? " — high on the page (estimated from document order; no browser rendering here)."
                : " — low on the page (estimated from document order; no browser rendering here).");
        if (c.aboveFold()) {
            return pass(evidence, "Nothing to do.");
        }
        return partial(evidence, "Move the booking button into the header or the first screen so it is visible without scrolling.");
    }
}
