package com.antenapro.hotelcheck.sitecheck.check;

import com.antenapro.hotelcheck.sitecheck.html.BookingCta;
import com.antenapro.hotelcheck.sitecheck.html.HtmlSupport;
import com.antenapro.hotelcheck.sitecheck.model.Finding;
import com.antenapro.hotelcheck.sitecheck.model.SiteSnapshot;
import com.antenapro.hotelcheck.sitecheck.model.Stage;
import org.jsoup.nodes.Document;

import java.util.List;

/** PASS: a booking CTA leads to the site's own host or a known booking engine, not only to an OTA. */
class OwnBookingEngineCheck extends AbstractSiteCheck {

    OwnBookingEngineCheck() {
        super("own-booking-engine", "Own booking engine", Stage.BOOK,
                "Bookings sent to an OTA pay 15–25 % commission and hand the guest relationship to the OTA.", true);
    }

    @Override
    protected Finding check(SiteSnapshot snapshot, Document home) {
        List<BookingCta.Cta> ctas = BookingCta.findAll(home);
        if (ctas.isEmpty()) {
            return unknown("No booking call to action on the home page (see \"Booking call to action\").", "Add a booking button first.");
        }
        String site = snapshot.host();
        List<BookingCta.Cta> own = ctas.stream().filter(c -> c.href().startsWith("http") && !BookingEngines.isOta(c.href())
                && (HtmlSupport.host(c.href()).equals(site) || BookingEngines.isEngine(c.href()))).toList();
        List<BookingCta.Cta> otas = ctas.stream().filter(c -> BookingEngines.isOta(c.href())).toList();
        List<BookingCta.Cta> messaging = ctas.stream().filter(BookingCta.Cta::isMessagingOrCall).toList();
        List<BookingCta.Cta> buttons = ctas.stream().filter(c -> c.href().isEmpty()).toList();
        if (!own.isEmpty()) {
            return pass("Booking goes to " + HtmlSupport.shorten(own.get(0).href(), 80) + ".", "Nothing to do.");
        }
        if (!buttons.isEmpty()) {
            return pass("Booking opens on the page itself: " + buttons.get(0).describe() + ".", "Nothing to do.");
        }
        if (!otas.isEmpty() && messaging.isEmpty()) {
            return fail("The only booking links go to " + otas.stream().map(c -> HtmlSupport.host(c.href())).distinct().toList() + ".",
                    "Add a direct booking engine on your own domain so guests can book without OTA commission.");
        }
        if (!messaging.isEmpty()) {
            return partial("Booking is by " + (BookingCta.isWhatsApp(messaging.get(0).href()) ? "WhatsApp" : "phone") + " only: " + messaging.get(0).describe()
                    + (otas.isEmpty() ? "." : "; OTA links also present."),
                    "Keep WhatsApp, and add a booking page on your own domain that shows dates, rooms and prices.");
        }
        return partial("Booking goes to " + HtmlSupport.shorten(ctas.get(0).href(), 80) + ", a host we do not recognise as a booking engine.",
                "If that is your booking engine, nothing to do; if it is a listing site, add direct booking on your own domain.");
    }
}
