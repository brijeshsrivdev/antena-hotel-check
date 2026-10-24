package com.antenapro.hotelcheck.sitecheck.psi;

import com.antenapro.hotelcheck.sitecheck.SiteCheckProperties;
import com.antenapro.hotelcheck.sitecheck.model.PsiResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Calls {@code https://www.googleapis.com/pagespeedonline/v5/runPagespeed} with the mobile strategy. Without a
 * key ({@code hotelcheck.sitecheck.psi-key}) nothing is called and the result says so: Google's keyless quota
 * is zero (observed 24 Oct 2026), the free keyed quota is 25k/day.
 */
public class HttpPsiClient implements PsiClient {

    public static final String NO_KEY_LIMITATION =
            "No PageSpeed Insights key is configured (hotelcheck.sitecheck.psi-key), so mobile-friendliness, Core Web Vitals and page weight are unknown.";
    static final String ENDPOINT = "https://www.googleapis.com/pagespeedonline/v5/runPagespeed";

    private static final Logger log = LoggerFactory.getLogger(HttpPsiClient.class);

    private final SiteCheckProperties properties;
    private final HttpClient client;
    private final String endpoint;

    public HttpPsiClient(SiteCheckProperties properties) {
        this(properties, ENDPOINT);
    }

    HttpPsiClient(SiteCheckProperties properties, String endpoint) {
        this.properties = properties;
        this.endpoint = endpoint;
        this.client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }

    @Override
    public PsiResult analyze(URI url) {
        String key = properties.psiKey() == null ? "" : properties.psiKey().trim();
        if (key.isEmpty()) {
            return PsiResult.unavailable(NO_KEY_LIMITATION);
        }
        String query = "?url=" + URLEncoder.encode(url.toString(), StandardCharsets.UTF_8)
                + "&strategy=mobile&category=performance&key=" + URLEncoder.encode(key, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint + query))
                .GET()
                .timeout(Duration.ofMillis(properties.psiTimeoutMs()))
                .header("Accept", "application/json")
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            PsiResult result = PsiResponseParser.parse(response.body());
            if (!result.available()) {
                log.warn("PageSpeed Insights unavailable for {}: HTTP {} - {}", url.getHost(), response.statusCode(), result.limitation());
            }
            return result;
        } catch (HttpTimeoutException e) {
            return PsiResult.unavailable("PageSpeed Insights did not answer within " + properties.psiTimeoutMs() / 1000
                    + " seconds; mobile-friendliness, Core Web Vitals and page weight are unknown.");
        } catch (IOException e) {
            log.warn("PageSpeed Insights call failed for {}: {}", url.getHost(), e.toString());
            return PsiResult.unavailable("PageSpeed Insights could not be reached (" + e.getClass().getSimpleName() + ").");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return PsiResult.unavailable("PageSpeed Insights call was interrupted.");
        }
    }
}
