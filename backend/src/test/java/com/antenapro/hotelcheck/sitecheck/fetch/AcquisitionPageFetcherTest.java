package com.antenapro.hotelcheck.sitecheck.fetch;

import com.antenapro.hotelcheck.acquisition.AcquisitionConfig;
import com.antenapro.hotelcheck.acquisition.HttpPublicWebAcquisitionService;
import com.antenapro.hotelcheck.sitecheck.model.FetchedPage;
import com.antenapro.hotelcheck.sitecheck.model.PageRole;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The adapter over the REQ-015 acquisition service, against a loopback server (allowLocalhost in tests only). */
class AcquisitionPageFetcherTest {

    private HttpServer server;
    private String baseUrl;
    private AcquisitionPageFetcher fetcher;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
        server.start();
        AcquisitionConfig config = AcquisitionConfig.builder().allowLocalhost(true)
                .connectTimeout(Duration.ofSeconds(2)).readTimeout(Duration.ofSeconds(2)).maxResponseSizeBytes(64 * 1024).build();
        fetcher = new AcquisitionPageFetcher(new HttpPublicWebAcquisitionService(), config);
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    private void serve(String path, int status, String contentType, String body, String... headerPairs) {
        server.createContext(path, exchange -> {
            exchange.getResponseHeaders().set("Content-Type", contentType);
            for (int i = 0; i + 1 < headerPairs.length; i += 2) {
                exchange.getResponseHeaders().set(headerPairs[i], headerPairs[i + 1]);
            }
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });
    }

    @Test
    void fetch_ShouldParseHtml_AndCarryStatusTimingAndRobotsHeader() {
        serve("/", 200, "text/html; charset=utf-8", "<html><head><title>Hotel</title></head><body><h1>Hotel</h1></body></html>",
                "X-Robots-Tag", "noindex");

        FetchedPage page = fetcher.fetch(URI.create(baseUrl + "/"), PageRole.HOME);

        assertTrue(page.ok());
        assertEquals(200, page.status());
        assertEquals(baseUrl + "/", page.finalUrl());
        assertNotNull(page.document());
        assertEquals("Hotel", page.document().title());
        assertEquals("noindex", page.header("x-robots-tag").orElseThrow());
        assertTrue(page.ms() >= 0);
        assertTrue(page.bytes() > 0);
    }

    @Test
    void fetch_ShouldReportHttpErrors_WithoutADocument() {
        serve("/missing", 404, "text/html", "<html><body>gone</body></html>");

        FetchedPage page = fetcher.fetch(URI.create(baseUrl + "/missing"), PageRole.ROOMS);

        assertFalse(page.ok());
        assertTrue(page.responded());
        assertEquals(404, page.status());
        assertNull(page.document());
        assertEquals("HTTP 404", page.outcome());
    }

    @Test
    void fetch_ShouldReportAConnectionFailure_AsAFailedFetch() {
        server.stop(0);

        FetchedPage page = fetcher.fetch(URI.create(baseUrl + "/"), PageRole.HOME);

        assertFalse(page.responded());
        assertEquals(0, page.status());
        assertNotNull(page.error());
    }

    @Test
    void fetch_ShouldRefuseABodyOverTheCap_AndSaySo() {
        serve("/huge", 200, "text/html", "<html>" + "x".repeat(70 * 1024) + "</html>");

        FetchedPage page = fetcher.fetch(URI.create(baseUrl + "/huge"), PageRole.HOME);

        assertFalse(page.responded());
        assertTrue(page.error().contains("larger than"));
    }

    @Test
    void fetch_ShouldNotParseNonHtml_ButFetchTextKeepsIt() {
        serve("/robots.txt", 200, "text/plain", "User-agent: *\nDisallow:");

        FetchedPage page = fetcher.fetch(URI.create(baseUrl + "/robots.txt"), PageRole.HOME);
        assertEquals(200, page.status());
        assertFalse(page.ok());
        assertNull(page.document());
        assertNull(page.text());

        FetchedPage text = fetcher.fetchText(URI.create(baseUrl + "/robots.txt"), PageRole.ROBOTS);
        assertEquals("User-agent: *\nDisallow:", text.text());
        assertNull(text.document());
    }

    @Test
    void probe_ShouldReportStatusAndTiming_WithoutABody() {
        serve("/old", 410, "text/html", "<html>gone</html>");

        FetchedPage probe = fetcher.probe(URI.create(baseUrl + "/old"));

        assertEquals(PageRole.LINK, probe.role());
        assertEquals(410, probe.status());
        assertNull(probe.document());
        assertNull(probe.text());
    }
}
