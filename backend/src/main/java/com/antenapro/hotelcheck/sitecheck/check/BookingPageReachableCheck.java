package com.antenapro.hotelcheck.sitecheck.check;

import com.antenapro.hotelcheck.sitecheck.html.BookingCta;
import com.antenapro.hotelcheck.sitecheck.html.HtmlSupport;
import com.antenapro.hotelcheck.sitecheck.model.FetchedPage;
import com.antenapro.hotelcheck.sitecheck.model.Finding;
import com.antenapro.hotelcheck.sitecheck.model.PageRole;
import com.antenapro.hotelcheck.sitecheck.model.SiteSnapshot;
import com.antenapro.hotelcheck.sitecheck.model.Stage;
import org.jsoup.nodes.Document;

import java.util.Optional;

/** PASS: the booking CTA's target answers 200 within 3 s and shows a date picker or a form. */
class BookingPageReachableCheck extends AbstractSiteCheck {

    private static final int MAX_MS = 3_000;

    BookingPageReachableCheck() {
        super("booking-page-reachable", "Booking page reachable", Stage.BOOK,
                "A booking button that leads to a slow or broken page loses the guest at the last step.", true);
    }

    @Override
    protected Finding check(SiteSnapshot snapshot, Document home) {
        Optional<BookingCta.Cta> cta = BookingCta.find(home);
        if (cta.isEmpty()) {
            return unknown("No booking call to action on the home page (see \"Booking call to action\").", "Add a booking button first.");
        }
        if (cta.get().isMessagingOrCall()) {
            return partial("The booking button opens " + (BookingCta.isWhatsApp(cta.get().href()) ? "WhatsApp" : "the phone dialer")
                    + " (" + HtmlSupport.shorten(cta.get().href(), 60) + "); there is no booking page with dates and prices.",
                    "Add a booking page that shows availability and prices, and keep WhatsApp as the second option.");
        }
        Optional<FetchedPage> page = snapshot.page(PageRole.BOOKING);
        if (page.isEmpty()) {
            if (cta.get().href().isEmpty()) {
                return pass("The booking control is on the home page itself: " + cta.get().describe() + ".", "Nothing to do.");
            }
            return unknown("The booking target " + HtmlSupport.shorten(cta.get().href(), 80) + " was not fetched within the run's page or time budget.", "Run the check again.");
        }
        FetchedPage p = page.get();
        if (!p.ok()) {
            return fail("The booking target " + HtmlSupport.shorten(p.requestedUrl(), 80) + " answered "
                    + (p.error() != null ? p.error() : "HTTP " + p.status()) + " after " + p.ms() + " ms.",
                    "Fix the booking page so it loads; test the button on a phone.");
        }
        Document doc = p.document();
        boolean datePicker = !doc.select("input[type=date], input[name~=(?i)check.?in|arrival|date], input[id~=(?i)check.?in|arrival|date], input[placeholder~=(?i)check.?in|arrival|date], [class~=(?i)datepicker|date-picker|calendar]").isEmpty();
        boolean form = !doc.select("form").isEmpty();
        String evidence = HtmlSupport.shorten(p.finalUrl(), 80) + " answered HTTP " + p.status() + " in " + p.ms() + " ms; "
                + (datePicker ? "date picker found" : form ? "a form but no date picker" : "no form or date picker") + ".";
        if (p.ms() <= MAX_MS && (datePicker || form)) {
            return pass(evidence, "Nothing to do.");
        }
        if (!datePicker && !form) {
            return partial(evidence, "Put a date and room selector on the booking page so the guest can see availability straight away.");
        }
        return partial(evidence, "Speed up the booking page to load within 3 seconds; compress images and remove unused scripts.");
    }
}
