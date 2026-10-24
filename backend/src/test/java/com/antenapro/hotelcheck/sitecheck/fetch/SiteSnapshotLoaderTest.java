package com.antenapro.hotelcheck.sitecheck.fetch;

import com.antenapro.hotelcheck.sitecheck.SiteCheckProperties;
import com.antenapro.hotelcheck.sitecheck.Snapshots;
import com.antenapro.hotelcheck.sitecheck.model.PageRole;
import com.antenapro.hotelcheck.sitecheck.model.SiteSnapshot;
import org.junit.jupiter.api.Test;

import com.antenapro.hotelcheck.sitecheck.model.PsiResult;
import com.antenapro.hotelcheck.sitecheck.psi.PsiClient;
import org.junit.jupiter.api.AfterEach;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

class SiteSnapshotLoaderTest {

    private static final String HOME = "https://hotel.example.com/";
    private static final String HOME_HTML = "<html><head><title>Hotel Example</title></head><body>"
            + "<header><a href=\"/\">Home</a><a href=\"/rooms\">Rooms</a><a href=\"/contact\">Contact</a><a href=\"/about\">About</a>"
            + "<a class=\"btn\" href=\"/book\">Book Now</a></header></body></html>";

    private final ExecutorService side = Executors.newCachedThreadPool();

    @AfterEach
    void tearDown() {
        side.shutdownNow();
    }

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
        return loader(fetcher, maxPages, new Snapshots.FakePsiClient(PsiResult.unavailable("no key")));
    }

    private SiteSnapshotLoader loader(Snapshots.FakeFetcher fetcher, int maxPages, PsiClient psi) {
        return new SiteSnapshotLoader(SiteCheckProperties.defaults().withMaxPages(maxPages), fetcher, policy, psi, side, Clock.systemUTC());
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

        assertThat(fetcher.requested).containsSubsequence(HOME, HOME + "rooms", HOME + "contact", HOME + "book", HOME + "about");
        assertThat(s.pages()).containsKeys(PageRole.HOME, PageRole.ROOMS, PageRole.CONTACT, PageRole.BOOKING, PageRole.ABOUT);
        assertThat(s.fetched()).hasSize(7); // 5 pages + robots.txt + sitemap.xml (both 404 here)
        assertThat(s.limitations()).singleElement().asString().contains("no key");
        assertThat(s.robotsResult()).isPresent();
        assertThat(s.robotsResult().get().present()).isFalse();
        assertThat(s.sitemapResult()).isPresent();
        assertThat(s.psiResult()).isPresent();
        assertThat(s.linkProbes()).isEmpty();
    }

    @Test
    void load_ShouldStopAtThePageBudget_AndSayWhichPagesItSkipped() {
        Snapshots.FakeFetcher fetcher = new Snapshots.FakeFetcher().serve(HOME, HOME_HTML)
                .serve(HOME + "rooms", "<html><body>rooms</body></html>").serve(HOME + "contact", "<html><body>contact</body></html>");

        SiteSnapshot s = loader(fetcher, 3).load(URI.create(HOME), later());

        assertThat(fetcher.requested).containsSubsequence(HOME, HOME + "rooms", HOME + "contact").doesNotContain(HOME + "book", HOME + "about");
        assertThat(s.pages()).doesNotContainKeys(PageRole.BOOKING, PageRole.ABOUT);
        assertThat(s.limitations()).filteredOn(l -> l.contains("at most 3 pages")).hasSize(2);
    }

    @Test
    void load_ShouldNotStartNewFetchesAfterTheDeadline() {
        Snapshots.FakeFetcher fetcher = new Snapshots.FakeFetcher().serve(HOME, HOME_HTML);

        SiteSnapshot s = loader(fetcher, 5).load(URI.create(HOME), Instant.now().minusSeconds(1));

        assertThat(fetcher.requested).filteredOn(u -> !u.endsWith("robots.txt") && !u.endsWith("sitemap.xml")).containsExactly(HOME);
        assertThat(s.limitations()).filteredOn(l -> l.contains("time limit")).hasSizeGreaterThanOrEqualTo(5); // 4 pages + link sampling
        assertThat(s.linkProbes()).isNull();
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

        assertThat(fetcher.requested).filteredOn(u -> !u.endsWith("robots.txt") && !u.endsWith("sitemap.xml")).containsExactly(HOME);
        assertThat(s.home().ok()).isFalse();
        assertThat(s.limitations()).singleElement().asString().contains("could not be fetched");
        assertThat(s.robotsResult()).isEmpty();
        assertThat(s.psiResult()).isEmpty();
    }

    @Test
    void load_ShouldReadRobotsAndTheSitemapItNames_AndFollowAnIndexToItsFirstChild() {
        Snapshots.FakeFetcher fetcher = new Snapshots.FakeFetcher().serve(HOME, HOME_HTML)
                .serveText(HOME + "robots.txt", "User-agent: *\nAllow: /\nSitemap: https://hotel.example.com/sitemap_index.xml\n")
                .serveText(HOME + "sitemap_index.xml", "<sitemapindex xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"><sitemap><loc>https://hotel.example.com/page-sitemap.xml</loc></sitemap></sitemapindex>")
                .serveText(HOME + "page-sitemap.xml", "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"><url><loc>https://hotel.example.com/</loc><lastmod>2026-10-01</lastmod></url><url><loc>https://hotel.example.com/rooms</loc></url></urlset>");

        SiteSnapshot s = loader(fetcher, 5).load(URI.create(HOME), later());

        assertThat(s.robotsResult().get().sitemapUrls()).containsExactly("https://hotel.example.com/sitemap_index.xml");
        assertThat(s.sitemapResult().get().isIndex()).isTrue();
        assertThat(s.sitemapResult().get().urlCount()).isEqualTo(2);
        assertThat(s.sitemapResult().get().latestLastmod()).isEqualTo(java.time.LocalDate.of(2026, 10, 1));
        assertThat(fetcher.requested).contains(HOME + "robots.txt", HOME + "sitemap_index.xml", HOME + "page-sitemap.xml");
    }

    @Test
    void load_ShouldProbeOtherInternalLinks_UpToTheLimit_SkippingPagesAlreadyFetched() {
        String html = HOME_HTML.replace("</header>", "<a href=\"/gallery\">Gallery</a><a href=\"/rooms\">Rooms again</a><a href=\"/offers\">Offers</a>"
                + "<a href=\"/blog/1\">1</a><a href=\"/blog/2\">2</a><a href=\"/blog/3\">3</a><a href=\"/blog/4\">4</a><a href=\"/blog/5\">5</a><a href=\"/blog/6\">6</a><a href=\"mailto:x@y\">mail</a></header>");
        Snapshots.FakeFetcher fetcher = new Snapshots.FakeFetcher().serve(HOME, html).serve(HOME + "gallery", "<html></html>").status(HOME + "offers", 410);

        SiteSnapshot s = loader(fetcher, 5).load(URI.create(HOME), later());

        assertThat(s.linkProbes()).hasSize(6);
        assertThat(fetcher.probed).doesNotContain(HOME + "rooms").contains(HOME + "gallery", HOME + "offers");
        assertThat(s.linkProbes().stream().filter(p -> p.status() == 410)).hasSize(1);
    }

    @Test
    void load_ShouldReportPsiAsALimitation_WhenItDoesNotAnswerBeforeTheDeadline_AndNotCancelIt() throws Exception {
        CountDownLatch release = new CountDownLatch(1);
        PsiClient slow = url -> {
            try {
                release.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return PsiResult.unavailable("late");
        };
        Snapshots.FakeFetcher fetcher = new Snapshots.FakeFetcher().serve(HOME, HOME_HTML);

        SiteSnapshot s = loader(fetcher, 5, slow).load(URI.create(HOME), Instant.now().plusMillis(300));
        release.countDown();

        assertThat(s.psiResult()).isEmpty();
        assertThat(s.limitations()).anySatisfy(l -> assertThat(l).contains("PageSpeed Insights did not answer within the time limit"));
        assertThat(s.robotsResult()).isPresent();
    }
}
