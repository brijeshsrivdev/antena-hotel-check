package com.antenapro.hotelcheck.sitecheck;

import com.antenapro.hotelcheck.sitecheck.check.SiteChecks;
import com.antenapro.hotelcheck.sitecheck.fetch.SiteSnapshotLoader;
import com.antenapro.hotelcheck.sitecheck.fetch.UrlPolicy;
import com.antenapro.hotelcheck.sitecheck.model.AnalysisResult;
import com.antenapro.hotelcheck.sitecheck.model.CheckStatus;
import com.antenapro.hotelcheck.sitecheck.model.Finding;
import com.antenapro.hotelcheck.sitecheck.model.SiteCheckRun;
import com.antenapro.hotelcheck.sitecheck.model.Stage;
import com.antenapro.hotelcheck.sitecheck.api.SiteCheckResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import com.antenapro.hotelcheck.sitecheck.model.PsiResult;
import com.antenapro.hotelcheck.sitecheck.psi.PsiResponseParser;

import java.net.InetAddress;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.antenapro.hotelcheck.sitecheck.model.CheckStatus.FAIL;
import static com.antenapro.hotelcheck.sitecheck.model.CheckStatus.PARTIAL;
import static com.antenapro.hotelcheck.sitecheck.model.CheckStatus.PASS;
import static com.antenapro.hotelcheck.sitecheck.model.CheckStatus.UNKNOWN;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The three sites the plan names: an Antena storefront, a Wix site that only links to OTAs, and a WordPress site
 * with a booking plugin. Pages come from fixtures through the real loader; the analyzer runs the full catalogue.
 */
class SiteAnalyzerFixtureTest {

    private static final UrlPolicy PUBLIC = new UrlPolicy(host -> new InetAddress[]{InetAddress.getByName("93.184.216.34")});
    private final SiteAnalyzer analyzer = new SiteAnalyzer(SiteChecks.all());

    private static final ExecutorService SIDE = Executors.newCachedThreadPool();

    private AnalysisResult analyze(String url, Snapshots.FakeFetcher fetcher) {
        return analyze(url, fetcher, PsiResult.unavailable("No PageSpeed Insights key is configured (test)."));
    }

    private AnalysisResult analyze(String url, Snapshots.FakeFetcher fetcher, PsiResult psi) {
        SiteSnapshotLoader loader = new SiteSnapshotLoader(SiteCheckProperties.defaults(), fetcher, PUBLIC,
                new Snapshots.FakePsiClient(psi), SIDE, Clock.systemUTC());
        return analyzer.analyze(loader.load(URI.create(url), Instant.now().plusSeconds(20)));
    }

    private static Map<String, CheckStatus> statuses(AnalysisResult r) {
        return r.findings().stream().collect(Collectors.toMap(Finding::check, Finding::status));
    }

    @Test
    void antenaStorefront_ShouldPassEveryCheck_AndScore100() {
        Snapshots.FakeFetcher fetcher = new Snapshots.FakeFetcher()
                .serve("https://lexisinnbeachresort.com/", Snapshots.fixture("antena-storefront.html"))
                .serve("https://lexisinnbeachresort.com/rooms", Snapshots.fixture("antena-storefront-rooms.html"))
                .serve("https://lexisinnbeachresort.com/contact", "<html lang=en><head><title>Contact Lexis Inn Beach Resort, Diveagar</title><meta name=description content=\"Call, WhatsApp or email Lexis Inn Beach Resort in Diveagar; directions from Pune and Mumbai, parking and check-in times for your stay.\"><link rel=canonical href=\"https://lexisinnbeachresort.com/contact\"></head><body><h1>Contact</h1><a href=\"tel:+919822012345\">Call</a></body></html>")
                .serve("https://lexisinnbeachresort.com/about", "<html lang=en><head><title>About Lexis Inn Beach Resort, Diveagar</title><meta name=description content=\"The family behind Lexis Inn Beach Resort in Diveagar, the garden, the kitchen and the beach two minutes away; our story since 2012.\"><link rel=canonical href=\"https://lexisinnbeachresort.com/about\"></head><body><h1>About</h1></body></html>")
                .serveText("https://lexisinnbeachresort.com/robots.txt", Snapshots.fixture("robots-allow.txt"))
                .serveText("https://lexisinnbeachresort.com/sitemap.xml", Snapshots.fixture("sitemap-urlset.xml"));
        AnalysisResult r = analyze("https://lexisinnbeachresort.com/", fetcher, PsiResponseParser.parse(Snapshots.fixture("psi/mobile-success.json")));

        Map<String, CheckStatus> s = statuses(r);
        assertThat(s).hasSize(27);
        assertThat(s.entrySet().stream().filter(e -> e.getValue() != PASS).toList())
                .as("findings that are not PASS: %s", r.findings().stream().filter(f -> f.status() != PASS).toList())
                .isEmpty();
        assertThat(r.score()).isEqualTo(100);
        assertThat(r.stages().values()).allSatisfy(st -> assertThat(st.score()).isEqualTo(100));
        assertThat(r.findings()).allSatisfy(f -> assertThat(f.evidence()).isNotBlank().hasSizeLessThanOrEqualTo(Finding.MAX_EVIDENCE_CHARS));
        assertThat(r.findings()).allSatisfy(f -> assertThat(f.fix()).isNotBlank());
        // The button the reference repo missed is the booking CTA evidence.
        assertThat(r.findings().stream().filter(f -> f.check().equals("booking-cta")).findFirst().orElseThrow().evidence())
                .contains("<button>Check Availability</button>");
        assertThat(r.findings().stream().filter(f -> f.check().equals("core-web-vitals")).findFirst().orElseThrow().evidence())
                .contains("field data").contains("LCP 1.9 s (good)");
        assertThat(r.limitations()).isEmpty();
    }

    @Test
    void wixOtaOnlySite_ShouldFailBookingAndTrustChecks() {
        Snapshots.FakeFetcher fetcher = new Snapshots.FakeFetcher()
                .serve("https://www.seashellresortdiveagar.com/", Snapshots.fixture("wix-ota-only.html"))
                .serve("https://www.booking.com/hotel/in/sea-shell-resort-diveagar.html", "<html><body><form><input type=date name=checkin></form></body></html>");
        AnalysisResult r = analyze("https://www.seashellresortdiveagar.com/", fetcher, PsiResponseParser.parse(Snapshots.fixture("psi/mobile-poor-lab-only.json")));

        Map<String, CheckStatus> s = statuses(r);
        assertThat(s).hasSize(27)
                .containsEntry("robots", PARTIAL)
                .containsEntry("sitemap", FAIL)
                .containsEntry("mobile-friendly", FAIL)
                .containsEntry("core-web-vitals", FAIL)
                .containsEntry("page-weight", FAIL)
                .containsEntry("broken-links", FAIL)
                .containsEntry("https", PASS)
                .containsEntry("title", PARTIAL)
                .containsEntry("meta-description", PARTIAL)
                .containsEntry("canonical", FAIL)
                .containsEntry("indexable", PASS)
                .containsEntry("single-h1", PARTIAL)
                .containsEntry("hotel-name-consistency", FAIL)
                .containsEntry("rooms-page", FAIL)
                .containsEntry("content-depth", FAIL)
                .containsEntry("language", FAIL)
                .containsEntry("internal-links", PARTIAL)
                .containsEntry("image-alt", FAIL)
                .containsEntry("structured-data", FAIL)
                .containsEntry("rating-honesty", FAIL)
                .containsEntry("address-phone", PARTIAL)
                .containsEntry("gbp-link", FAIL)
                .containsEntry("last-updated", FAIL)
                .containsEntry("booking-cta", PARTIAL)
                .containsEntry("own-booking-engine", FAIL)
                .containsEntry("tap-targets", FAIL)
                .containsEntry("booking-page-reachable", PASS);
        assertThat(r.score()).isLessThan(30);
        assertThat(r.findings().stream().filter(f -> f.check().equals("broken-links")).findFirst().orElseThrow().evidence())
                .contains("/gallery").contains("HTTP 404");
        assertThat(r.findings().stream().filter(f -> f.check().equals("core-web-vitals")).findFirst().orElseThrow().evidence())
                .contains("lab data").contains("LCP 6.8 s (poor)").contains("INP not reported");
        assertThat(r.findings().get(0).status()).isEqualTo(FAIL);
        assertThat(r.findings().stream().filter(f -> f.check().equals("own-booking-engine")).findFirst().orElseThrow().evidence())
                .contains("booking.com");
        assertThat(r.findings().stream().filter(f -> f.check().equals("structured-data")).findFirst().orElseThrow().evidence())
                .contains("WebSite, Organization");
    }

    @Test
    void wordpressSiteWithBookingPlugin_ShouldPassBookingButFlagThinContentAndMixedContent() {
        Snapshots.FakeFetcher fetcher = new Snapshots.FakeFetcher()
                .serve("https://betelnutresort.in/", Snapshots.fixture("wordpress-booking.html"))
                .serve("https://betelnutresort.in/booking/", Snapshots.fixture("wordpress-booking-page.html"))
                .serveText("https://betelnutresort.in/robots.txt", "User-agent: *\nDisallow: /wp-admin/\n")
                .serveText("https://betelnutresort.in/sitemap.xml", Snapshots.fixture("sitemap-index.xml"))
                .serveText("https://betelnutresort.in/page-sitemap.xml", Snapshots.fixture("sitemap-pages.xml"));
        AnalysisResult r = analyze("https://betelnutresort.in/", fetcher, PsiResponseParser.parse(Snapshots.fixture("psi/quota-exceeded.json")));

        Map<String, CheckStatus> s = statuses(r);
        assertThat(s).hasSize(27)
                .containsEntry("robots", PASS)
                .containsEntry("sitemap", PASS)
                .containsEntry("mobile-friendly", PARTIAL)
                .containsEntry("core-web-vitals", UNKNOWN)
                .containsEntry("page-weight", UNKNOWN)
                .containsEntry("broken-links", FAIL)
                .containsEntry("https", PARTIAL)
                .containsEntry("title", PASS)
                .containsEntry("meta-description", PASS)
                .containsEntry("canonical", PASS)
                .containsEntry("indexable", PASS)
                .containsEntry("single-h1", PASS)
                .containsEntry("hotel-name-consistency", PASS)
                .containsEntry("rooms-page", FAIL)
                .containsEntry("content-depth", PARTIAL)
                .containsEntry("language", PASS)
                .containsEntry("internal-links", PASS)
                .containsEntry("image-alt", PARTIAL)
                .containsEntry("structured-data", FAIL)
                .containsEntry("rating-honesty", PASS)
                .containsEntry("address-phone", PASS)
                .containsEntry("gbp-link", FAIL)
                .containsEntry("last-updated", PARTIAL)
                .containsEntry("booking-cta", PASS)
                .containsEntry("own-booking-engine", PASS)
                .containsEntry("tap-targets", PARTIAL)
                .containsEntry("booking-page-reachable", PASS);
        assertThat(r.fetched()).hasSize(8); // 5 pages + robots + index + child sitemap; no other links to probe
        assertThat(r.limitations()).singleElement().asString().contains("quota exceeded");
        assertThat(r.findings().stream().filter(f -> f.check().equals("sitemap")).findFirst().orElseThrow().evidence())
                .contains("sitemap index with 2 sitemap(s)").contains("lists 2 URL(s)");
        assertThat(r.findings().stream().filter(f -> f.check().equals("rooms-page")).findFirst().orElseThrow().evidence())
                .contains("accommodation").contains("404");
        assertThat(r.stages().get(Stage.BOOK).score()).isEqualTo(88);
    }

    @Test
    void unreadableHome_ShouldReportEveryCheckUnknown_AndNoScore() {
        Snapshots.FakeFetcher fetcher = new Snapshots.FakeFetcher().failUnknown();
        AnalysisResult r = analyze("https://down.example.com/", fetcher);

        assertThat(r.findings()).hasSize(27).allSatisfy(f -> assertThat(f.status()).isEqualTo(UNKNOWN));
        assertThat(r.score()).isNull();
        assertThat(r.limitations()).anySatisfy(l -> assertThat(l).contains("could not be fetched"));
    }

    @Test
    void response_ShouldStayUnder200Kb_AndCarryNoPageHtml() throws Exception {
        Snapshots.FakeFetcher fetcher = new Snapshots.FakeFetcher()
                .serve("https://lexisinnbeachresort.com/", Snapshots.fixture("antena-storefront.html"))
                .serve("https://lexisinnbeachresort.com/rooms", Snapshots.fixture("antena-storefront-rooms.html"));
        AnalysisResult r = analyze("https://lexisinnbeachresort.com/", fetcher);
        SiteCheckRun run = new SiteCheckRun(UUID.randomUUID(), URI.create("https://lexisinnbeachresort.com/"), Instant.now());
        run.complete(r, Instant.now());

        String json = new ObjectMapper().registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).writeValueAsString(SiteCheckResponse.of(run));

        assertThat(json.length()).isLessThan(200 * 1024);
        assertThat(json).doesNotContain("<!DOCTYPE").doesNotContain("<body");
        assertThat(json).contains("\"score\":100").contains("\"stages\"").contains("\"findings\"").contains("\"fetched\"");
    }
}
