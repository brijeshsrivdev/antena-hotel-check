package com.antenapro.hotelcheck.sitecheck.fetch;

import com.antenapro.hotelcheck.sitecheck.SiteCheckProperties;
import com.antenapro.hotelcheck.sitecheck.Snapshots;
import com.antenapro.hotelcheck.sitecheck.model.PageRole;
import com.antenapro.hotelcheck.sitecheck.model.SiteSnapshot;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.time.Clock;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class SiteSnapshotLoaderTest {

    private static final String HOME = "https://hotel.example.com/";
    private static final String HOME_HTML = "<html><head><title>Hotel Example</title></head><body>"
            + "<header><a href=\"/\">Home</a><a href=\"/rooms\">Rooms</a><a href=\"/contact\">Contact</a><a href=\"/about\">About</a>"
            + "<a class=\"btn\" href=\"/book\">Book Now</a></header></body></html>";

    private final UrlPolicy policy = new UrlPolicy(host -> {
        if (host.equals("private.example.com")) {
            return new InetAddress[]{InetAddress.getByName("10.0.0.1")};
        }
        if (host.endsWith("example.com")) {
            return new InetAddress[]{InetAddress.getByName("93.184.216.34")};
        }
        throw new UnknownHostException(host);
    });

    private SiteSnapshotLoader loader(Snapshots.FakeFetcher fetcher, int maxPages) {
        return new SiteSnapshotLoader(SiteCheckProperties.defaults().withMaxPages(maxPages), fetcher, policy, Clock.systemUTC());
    }

    private static Instant later() {
        return Instant.now().plusSeconds(30);
    }

    @Test
    void load_ShouldFetchHomeThenRoomsContactBookingAbout_InThatOrder() {
        Snapshots.FakeFetcher fetcher = new Snapshots.FakeFetcher().serve(HOME, HOME_HTML)
                .serve(HOME + "rooms", "<html><body>rooms</body></html>").serve(HOME + "contact", "<html><body>contact</body></html>")
                .serve(HOME + "book", "<html><body>book</body></html>").serve(HOME + "about", "<html><body>about</body></html>");

        SiteSnapshot s = loader(fetcher, 5).load(URI.create(HOME), later());

        assertThat(fetcher.requested).containsExactly(HOME, HOME + "rooms", HOME + "contact", HOME + "book", HOME + "about");
        assertThat(s.pages()).containsKeys(PageRole.HOME, PageRole.ROOMS, PageRole.CONTACT, PageRole.BOOKING, PageRole.ABOUT);
        assertThat(s.fetched()).hasSize(5);
        assertThat(s.limitations()).isEmpty();
    }

    @Test
    void load_ShouldStopAtThePageBudget_AndSayWhichPagesItSkipped() {
        Snapshots.FakeFetcher fetcher = new Snapshots.FakeFetcher().serve(HOME, HOME_HTML)
                .serve(HOME + "rooms", "<html><body>rooms</body></html>").serve(HOME + "contact", "<html><body>contact</body></html>");

        SiteSnapshot s = loader(fetcher, 3).load(URI.create(HOME), later());

        assertThat(fetcher.requested).containsExactly(HOME, HOME + "rooms", HOME + "contact");
        assertThat(s.pages()).doesNotContainKeys(PageRole.BOOKING, PageRole.ABOUT);
        assertThat(s.limitations()).hasSize(2).allSatisfy(l -> assertThat(l).contains("at most 3 pages"));
    }

    @Test
    void load_ShouldNotStartNewFetchesAfterTheDeadline() {
        Snapshots.FakeFetcher fetcher = new Snapshots.FakeFetcher().serve(HOME, HOME_HTML);

        SiteSnapshot s = loader(fetcher, 5).load(URI.create(HOME), Instant.now().minusSeconds(1));

        assertThat(fetcher.requested).containsExactly(HOME);
        assertThat(s.limitations()).hasSize(4).allSatisfy(l -> assertThat(l).contains("time limit"));
    }

    @Test
    void load_ShouldMapAnOnPageBookingAnchorToTheHomePage_WithoutRefetching() {
        String html = HOME_HTML.replace("href=\"/book\"", "href=\"#booking\"");
        Snapshots.FakeFetcher fetcher = new Snapshots.FakeFetcher().serve(HOME, html);

        SiteSnapshot s = loader(fetcher, 5).load(URI.create(HOME), later());

        assertThat(fetcher.requested).doesNotContain(HOME + "#booking");
        assertThat(s.page(PageRole.BOOKING)).isPresent();
        assertThat(s.page(PageRole.BOOKING).get().finalUrl()).isEqualTo(HOME);
    }

    @Test
    void load_ShouldFetchAnExternalBookingEngine_ButNeverAPrivateHost() {
        String html = HOME_HTML.replace("href=\"/book\"", "href=\"https://private.example.com/engine\"");
        Snapshots.FakeFetcher fetcher = new Snapshots.FakeFetcher().serve(HOME, html);

        SiteSnapshot s = loader(fetcher, 5).load(URI.create(HOME), later());

        assertThat(fetcher.requested).doesNotContain("https://private.example.com/engine");
        assertThat(s.limitations()).anySatisfy(l -> assertThat(l).contains("booking link points to an address we do not fetch"));

        String engine = HOME_HTML.replace("href=\"/book\"", "href=\"https://engine.example.com/hotel\"");
        Snapshots.FakeFetcher fetcher2 = new Snapshots.FakeFetcher().serve(HOME, engine).serve("https://engine.example.com/hotel", "<html><body><form></form></body></html>");
        SiteSnapshot s2 = loader(fetcher2, 5).load(URI.create(HOME), later());
        assertThat(fetcher2.requested).contains("https://engine.example.com/hotel");
        assertThat(s2.page(PageRole.BOOKING)).isPresent();
    }

    @Test
    void load_ShouldStopAfterAFailedHomePage_AndExplain() {
        Snapshots.FakeFetcher fetcher = new Snapshots.FakeFetcher().failUnknown();

        SiteSnapshot s = loader(fetcher, 5).load(URI.create(HOME), later());

        assertThat(fetcher.requested).containsExactly(HOME);
        assertThat(s.home().ok()).isFalse();
        assertThat(s.limitations()).singleElement().asString().contains("could not be fetched");
    }
}
