package com.antenapro.hotelcheck.sitecheck.check;

import com.antenapro.hotelcheck.sitecheck.Snapshots;
import com.antenapro.hotelcheck.sitecheck.model.CheckStatus;
import com.antenapro.hotelcheck.sitecheck.model.Finding;
import com.antenapro.hotelcheck.sitecheck.model.PageRole;
import com.antenapro.hotelcheck.sitecheck.model.SiteSnapshot;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.antenapro.hotelcheck.sitecheck.model.CheckStatus.FAIL;
import static com.antenapro.hotelcheck.sitecheck.model.CheckStatus.PARTIAL;
import static com.antenapro.hotelcheck.sitecheck.model.CheckStatus.PASS;
import static com.antenapro.hotelcheck.sitecheck.model.CheckStatus.UNKNOWN;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The states the three site fixtures do not reach (SiteAnalyzerFixtureTest covers the rest), so every check
 * has a PASS, PARTIAL, FAIL and UNKNOWN case between the two classes.
 */
class CheckStatesTest {

    private static final String HOME = "https://hotel.example.com/";

    private static Finding run(String checkId, SiteSnapshot snapshot) {
        return SiteChecks.all().stream().filter(c -> c.id().equals(checkId)).findFirst().orElseThrow().run(snapshot);
    }

    private static Finding run(String checkId, String homeHtml) {
        return run(checkId, Snapshots.home(HOME, homeHtml));
    }

    private static String page(String head, String body) {
        return "<!doctype html><html lang=\"en\"><head><title>Hotel Example – Rooms in Diveagar</title>" + head + "</head><body>" + body + "</body></html>";
    }

    @Test
    void everyCheck_ShouldBeUnknown_WhenTheHomePageCannotBeRead() {
        SiteSnapshot snapshot = Snapshots.unreadableHome(HOME);
        for (SiteCheck check : SiteChecks.all()) {
            Finding f = check.run(snapshot);
            assertThat(f.status()).as(check.id()).isEqualTo(UNKNOWN);
            assertThat(f.evidence()).as(check.id()).isNotBlank();
        }
    }

    @Test
    void catalogue_ShouldHaveUniqueIds_AndACatalogueColumnForEachCheck() {
        assertThat(SiteChecks.all()).hasSize(21).extracting(SiteCheck::id).doesNotHaveDuplicates();
        assertThat(SiteChecks.all()).allSatisfy(c -> assertThat(c.title()).isNotBlank());
    }

    @Test
    void https_ShouldFail_WhenTheFinalUrlIsHttp() {
        Finding f = run("https", Snapshots.home("http://hotel.example.com/", page("", "<h1>Hotel Example</h1>")));
        assertThat(f.status()).isEqualTo(FAIL);
        assertThat(f.evidence()).contains("http://hotel.example.com/");
    }

    @Test
    void title_ShouldFail_WhenMissing_OrRepeatedAcrossPages() {
        assertThat(run("title", "<html><head></head><body><h1>Hotel Example</h1></body></html>").status()).isEqualTo(FAIL);
        String same = page("", "<h1>Hotel Example</h1>");
        SiteSnapshot twoPages = Snapshots.of(Snapshots.page(PageRole.HOME, HOME, same), Snapshots.page(PageRole.ROOMS, HOME + "rooms", same));
        Finding f = run("title", twoPages);
        assertThat(f.status()).isEqualTo(FAIL);
        assertThat(f.evidence()).contains("repeats");
    }

    @Test
    void metaDescription_ShouldFail_WhenMissing() {
        assertThat(run("meta-description", page("", "")).status()).isEqualTo(FAIL);
    }

    @Test
    void canonical_ShouldBePartial_WhenOnlyASubpagePointsElsewhere() {
        String home = page("<link rel=canonical href=\"" + HOME + "\">", "");
        String rooms = page("<link rel=canonical href=\"" + HOME + "\">", "");
        Finding f = run("canonical", Snapshots.of(Snapshots.page(PageRole.HOME, HOME, home), Snapshots.page(PageRole.ROOMS, HOME + "rooms", rooms)));
        assertThat(f.status()).isEqualTo(PARTIAL);
        assertThat(f.evidence()).contains("rooms canonical points to");
    }

    @Test
    void indexable_ShouldFailOnHomeNoindex_AndBePartialOnSubpageNoindexHeader() {
        assertThat(run("indexable", page("<meta name=robots content=\"noindex,follow\">", "")).status()).isEqualTo(FAIL);
        SiteSnapshot s = Snapshots.of(Snapshots.page(PageRole.HOME, HOME, page("", "")),
                Snapshots.page(PageRole.ROOMS, HOME + "rooms", 200, page("", ""), Map.of("x-robots-tag", "noindex")));
        Finding f = run("indexable", s);
        assertThat(f.status()).isEqualTo(PARTIAL);
        assertThat(f.evidence()).contains("X-Robots-Tag: noindex");
    }

    @Test
    void singleH1_ShouldFail_WhenThereIsNone() {
        assertThat(run("single-h1", page("", "<h2>Welcome</h2>")).status()).isEqualTo(FAIL);
    }

    @Test
    void hotelNameConsistency_ShouldBePartial_WhenOnlyTitleAndH1Agree() {
        Finding f = run("hotel-name-consistency", page("", "<h1>Hotel Example</h1>"));
        assertThat(f.status()).isEqualTo(PARTIAL);
        assertThat(f.evidence()).contains("Only 2 places");
    }

    @Test
    void roomsPage_ShouldBePartialWithOneRoomType_AndUnknownWhenTheLinkWasNotFetched() {
        String rooms = page("", "<h2>Deluxe Room</h2><p>₹3,000</p>");
        Finding partial = run("rooms-page", Snapshots.of(Snapshots.page(PageRole.HOME, HOME, page("", "")), Snapshots.page(PageRole.ROOMS, HOME + "rooms", rooms)));
        assertThat(partial.status()).isEqualTo(PARTIAL);
        Finding unknown = run("rooms-page", page("", "<a href=\"/rooms\">Rooms</a>"));
        assertThat(unknown.status()).isEqualTo(UNKNOWN);
    }

    @Test
    void language_ShouldBePartial_WhenTheTagIsNotValid() {
        Finding f = run("language", "<html lang=\"english language\"><head><title>x</title></head><body></body></html>");
        assertThat(f.status()).isEqualTo(PARTIAL);
    }

    @Test
    void internalLinks_ShouldFail_WhenNoneOfTheThreeExist() {
        assertThat(run("internal-links", page("", "<a href=\"/gallery\">Gallery</a>")).status()).isEqualTo(FAIL);
    }

    @Test
    void structuredData_ShouldBePartial_WhenHotelObjectMissesFields() {
        String head = "<script type=\"application/ld+json\">{\"@context\":\"https://schema.org\",\"@type\":\"Hotel\",\"name\":\"Hotel Example\",\"url\":\"https://other.example.net\"}</script>";
        Finding f = run("structured-data", page(head, ""));
        assertThat(f.status()).isEqualTo(PARTIAL);
        assertThat(f.evidence()).contains("address").contains("telephone").contains("image").contains("url (points to other.example.net");
    }

    @Test
    void ratingHonesty_ShouldBePartial_WhenCountIsStatedButNoReviewsAreVisible() {
        String head = "<script type=\"application/ld+json\">{\"@type\":\"Hotel\",\"name\":\"x\",\"aggregateRating\":{\"@type\":\"AggregateRating\",\"ratingValue\":4.5,\"reviewCount\":12}}</script>";
        Finding f = run("rating-honesty", page(head, "<p>Welcome</p>"));
        assertThat(f.status()).isEqualTo(PARTIAL);
        assertThat(f.evidence()).contains("reviewCount 12").contains("not found");
    }

    @Test
    void addressPhone_ShouldFail_WhenNeitherIsOnTheHomeOrContactPage() {
        assertThat(run("address-phone", page("", "<p>Welcome to our hotel</p>")).status()).isEqualTo(FAIL);
    }

    @Test
    void gbpLink_ShouldPassOnAnEmbeddedMap_OnTheContactPage() {
        SiteSnapshot s = Snapshots.of(Snapshots.page(PageRole.HOME, HOME, page("", "")),
                Snapshots.page(PageRole.CONTACT, HOME + "contact", page("", "<iframe src=\"https://www.google.com/maps/embed?pb=!1m18\"></iframe>")));
        Finding f = run("gbp-link", s);
        assertThat(f.status()).isEqualTo(PASS);
        assertThat(f.evidence()).contains("contact page");
    }

    @Test
    void lastUpdated_ShouldBeUnknown_WithoutACopyrightYear() {
        assertThat(run("last-updated", page("", "<footer>Made with love</footer>")).status()).isEqualTo(UNKNOWN);
    }

    @Test
    void bookingCta_ShouldFail_WhenNothingSaysBook() {
        assertThat(run("booking-cta", page("", "<a href=\"/gallery\">Gallery</a><button>Submit</button>")).status()).isEqualTo(FAIL);
    }

    @Test
    void ownBookingEngine_ShouldBePartialOnWhatsAppOnly_AndUnknownWithoutACta() {
        Finding wa = run("own-booking-engine", page("", "<a href=\"https://wa.me/919876543210\">Book on WhatsApp</a>"));
        assertThat(wa.status()).isEqualTo(PARTIAL);
        assertThat(wa.evidence()).contains("WhatsApp");
        assertThat(run("own-booking-engine", page("", "<p>no buttons</p>")).status()).isEqualTo(UNKNOWN);
    }

    @Test
    void bookingPageReachable_ShouldFailOnA404_PartialOnWhatsApp_UnknownWithoutACta() {
        String home = page("", "<header><a href=\"/book\">Book Now</a></header>");
        SiteSnapshot s = Snapshots.of(Snapshots.page(PageRole.HOME, HOME, home), Snapshots.page(PageRole.BOOKING, HOME + "book", 404, "", Map.of()));
        assertThat(run("booking-page-reachable", s).status()).isEqualTo(FAIL);
        assertThat(run("booking-page-reachable", page("", "<a href=\"https://wa.me/919876543210\">Book Now</a>")).status()).isEqualTo(PARTIAL);
        assertThat(run("booking-page-reachable", page("", "<p>nothing</p>")).status()).isEqualTo(UNKNOWN);
    }

    @Test
    void bookingPageReachable_ShouldBePartial_WhenThePageIsSlow() {
        String home = page("", "<header><a href=\"/book\">Book Now</a></header>");
        var slow = new com.antenapro.hotelcheck.sitecheck.model.FetchedPage(PageRole.BOOKING, HOME + "book", HOME + "book", 200, 4500, 100, false,
                Map.of("content-type", "text/html"), org.jsoup.Jsoup.parse("<form><input type=date></form>", HOME + "book"), null);
        Finding f = run("booking-page-reachable", Snapshots.of(Snapshots.page(PageRole.HOME, HOME, home), slow));
        assertThat(f.status()).isEqualTo(PARTIAL);
        assertThat(f.evidence()).contains("4500 ms");
    }

    @Test
    void evidence_ShouldNeverExceed300Characters() {
        StringBuilder many = new StringBuilder();
        for (int i = 0; i < 40; i++) {
            many.append("<img src=\"/a-very-long-image-file-name-number-").append(i).append(".jpg\">");
        }
        Finding f = run("image-alt", page("", many.toString()));
        assertThat(f.status()).isEqualTo(FAIL);
        assertThat(f.evidence().length()).isLessThanOrEqualTo(Finding.MAX_EVIDENCE_CHARS);
    }

    @Test
    void bookingCta_ShouldReadButtonsAndRoleButtons_NotOnlyAnchors() {
        Finding f = run("booking-cta", page("", "<div class=\"hero\"><span role=\"button\">Reserve now</span></div>"));
        assertThat(f.status()).isEqualTo(PASS);
        assertThat(f.evidence()).contains("<span>Reserve now</span>");
        assertThat(f.status()).isNotEqualTo(CheckStatus.UNKNOWN);
    }
}
