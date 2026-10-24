package com.antenapro.hotelcheck.sitecheck.fetch;

import com.antenapro.hotelcheck.acquisition.AcquisitionConfig;
import com.antenapro.hotelcheck.acquisition.AcquisitionRequest;
import com.antenapro.hotelcheck.acquisition.AcquisitionResult;
import com.antenapro.hotelcheck.acquisition.PublicWebAcquisitionService;
import com.antenapro.hotelcheck.sitecheck.SiteCheckProperties;
import com.antenapro.hotelcheck.sitecheck.model.FetchedPage;
import com.antenapro.hotelcheck.sitecheck.model.PageRole;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Fetches through the existing {@link PublicWebAcquisitionService} (REQ-015: SSRF-safe DNS resolution on
 * every hop, bounded redirects, response-size cap, timeouts) and parses HTML with jsoup. The service rejects
 * a body over the cap outright rather than truncating it, which is the right outcome for a 2.5 GB page: the
 * finding says the page could not be read.
 */
public class AcquisitionPageFetcher implements PageFetcher {

    private enum Mode { HTML, TEXT, PROBE }

    private final PublicWebAcquisitionService acquisition;
    private final AcquisitionConfig config;

    public AcquisitionPageFetcher(PublicWebAcquisitionService acquisition, SiteCheckProperties properties) {
        this(acquisition, AcquisitionConfig.builder()
                .connectTimeout(Duration.ofMillis(properties.connectTimeoutMs()))
                .readTimeout(Duration.ofMillis(properties.readTimeoutMs()))
                .maxResponseSizeBytes(properties.maxPageBytes())
                .maxRedirects(properties.maxRedirects())
                .userAgent(properties.userAgent())
                .build());
    }

    /** Lets tests point the fetcher at a loopback server through {@code allowLocalhost}. */
    public AcquisitionPageFetcher(PublicWebAcquisitionService acquisition, AcquisitionConfig config) {
        this.acquisition = acquisition;
        this.config = config;
    }

    @Override
    public FetchedPage fetch(URI url, PageRole role) {
        return fetch(url, role, Mode.HTML);
    }

    @Override
    public FetchedPage fetchText(URI url, PageRole role) {
        return fetch(url, role, Mode.TEXT);
    }

    @Override
    public FetchedPage probe(URI url) {
        return fetch(url, PageRole.LINK, Mode.PROBE);
    }

    private FetchedPage fetch(URI url, PageRole role, Mode mode) {
        long started = System.nanoTime();
        AcquisitionResult result = acquisition.acquire(new AcquisitionRequest(url.toString(), config));
        long ms = (System.nanoTime() - started) / 1_000_000;
        if (result.statusCode() == null) {
            return FetchedPage.failed(role, url.toString(), ms, describe(result));
        }
        Map<String, String> headers = new HashMap<>();
        if (result.contentType() != null) {
            headers.put("content-type", result.contentType());
        }
        for (Map.Entry<String, String> e : result.provenanceMetadata().entrySet()) {
            if (e.getKey().startsWith("header.")) {
                headers.put(e.getKey().substring("header.".length()).toLowerCase(Locale.ROOT), e.getValue());
            }
        }
        String body = result.body() == null ? "" : result.body();
        Document document = null;
        String text = null;
        if (mode == Mode.HTML && result.isSuccess() && isHtml(result.contentType(), body)) {
            document = Jsoup.parse(body, result.finalUrl());
        } else if (mode == Mode.TEXT && result.isSuccess() && body.indexOf('\0') < 0) {
            text = body;
        }
        return new FetchedPage(role, url.toString(), result.finalUrl(), result.statusCode(), ms, body.length(), false,
                headers, document, text, null);
    }

    private static boolean isHtml(String contentType, String body) {
        String ct = contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
        if (ct.contains("text/html") || ct.contains("application/xhtml")) {
            return true;
        }
        if (!ct.isBlank()) {
            return false;
        }
        String head = body.substring(0, Math.min(body.length(), 512)).toLowerCase(Locale.ROOT);
        return head.contains("<html") || head.contains("<!doctype html");
    }

    private static String describe(AcquisitionResult result) {
        return switch (result.outcome()) {
            case TIMEOUT -> "No response in time";
            case RESPONSE_TOO_LARGE -> "The page is larger than 2 MB and was not read";
            case REDIRECT_LIMIT_EXCEEDED -> "Too many redirects";
            case INVALID_TARGET -> "Redirected to an address we do not fetch";
            case UNSUPPORTED_SCHEME -> "Only http:// and https:// addresses can be fetched";
            case NETWORK_ERROR -> result.errorMessage() != null && result.errorMessage().contains("SSL")
                    ? "The HTTPS certificate could not be verified" : "Connection refused or timed out";
            default -> result.errorMessage() != null ? result.errorMessage() : "Could not fetch the page";
        };
    }
}
