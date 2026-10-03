package com.antenapro.hotelcheck.acquisition;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;

import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.util.Timeout;

import javax.net.ssl.SSLException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HttpPublicWebAcquisitionService implements PublicWebAcquisitionService {

    private final CloseableHttpClient customClient;

    public HttpPublicWebAcquisitionService() {
        this(null);
    }

    public HttpPublicWebAcquisitionService(CloseableHttpClient customClient) {
        this.customClient = customClient;
    }

    @Override
    public AcquisitionResult acquire(AcquisitionRequest request) {
        if (request == null) {
            return AcquisitionResult.failure(AcquisitionOutcome.INVALID_TARGET, null, "AcquisitionRequest cannot be null");
        }

        AcquisitionConfig config = request.config();
        String requestedUrl = request.targetUrl();

        AcquisitionUrlNormalizer.NormalizationResult initialNorm =
                AcquisitionUrlNormalizer.normalizeAndValidate(requestedUrl, config.allowLocalhost());

        if (!initialNorm.valid()) {
            return AcquisitionResult.failure(initialNorm.outcome(), requestedUrl, initialNorm.errorMessage());
        }

        String currentUrl = initialNorm.normalizedUrl();
        List<String> redirectChain = new ArrayList<>();
        int redirectCount = 0;

        CloseableHttpClient clientToUse = (this.customClient != null)
                ? this.customClient
                : createHttpClient(config);

        boolean isCustomClient = (this.customClient != null);

        try {
            while (true) {
                AcquisitionUrlNormalizer.NormalizationResult currentNorm =
                        AcquisitionUrlNormalizer.normalizeAndValidate(currentUrl, config.allowLocalhost());

                if (!currentNorm.valid()) {
                    return AcquisitionResult.failure(
                            currentNorm.outcome(),
                            requestedUrl,
                            currentUrl,
                            currentNorm.errorMessage(),
                            redirectChain
                    );
                }

                URI currentUri;
                try {
                    currentUri = URI.create(currentNorm.normalizedUrl());
                } catch (Exception e) {
                    return AcquisitionResult.failure(
                            AcquisitionOutcome.INVALID_TARGET,
                            requestedUrl,
                            currentUrl,
                            "Malformed target URI: " + e.getMessage(),
                            redirectChain
                    );
                }

                HttpGet httpGet = new HttpGet(currentUri);
                httpGet.setHeader("User-Agent", config.userAgent());
                httpGet.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");

                Instant retrievalTimestamp = Instant.now();

                try (CloseableHttpResponse response = clientToUse.execute(httpGet)) {
                    int statusCode = response.getCode();
                    Header contentTypeHeader = response.getFirstHeader("Content-Type");
                    String contentType = contentTypeHeader != null ? contentTypeHeader.getValue() : null;

                    // Handle 3xx Redirects
                    if (statusCode >= 300 && statusCode < 400) {
                        Header locationHeader = response.getFirstHeader("Location");
                        EntityUtils.consumeQuietly(response.getEntity());

                        if (locationHeader == null || locationHeader.getValue().isBlank()) {
                            return AcquisitionResult.failure(
                                    AcquisitionOutcome.HTTP_ERROR,
                                    requestedUrl,
                                    currentUrl,
                                    "Redirect response missing Location header (HTTP " + statusCode + ")",
                                    redirectChain
                            );
                        }

                        redirectChain.add(currentUrl);
                        redirectCount++;

                        if (redirectCount > config.maxRedirects()) {
                            return AcquisitionResult.failure(
                                    AcquisitionOutcome.REDIRECT_LIMIT_EXCEEDED,
                                    requestedUrl,
                                    currentUrl,
                                    "Redirect limit of " + config.maxRedirects() + " exceeded",
                                    redirectChain
                            );
                        }

                        try {
                            URI nextUri = currentUri.resolve(locationHeader.getValue());
                            currentUrl = nextUri.toString();
                        } catch (Exception e) {
                            return AcquisitionResult.failure(
                                    AcquisitionOutcome.INVALID_TARGET,
                                    requestedUrl,
                                    currentUrl,
                                    "Invalid redirect target location: " + locationHeader.getValue(),
                                    redirectChain
                            );
                        }
                        continue;
                    }

                    HttpEntity entity = response.getEntity();
                    if (entity != null && entity.getContentLength() > config.maxResponseSizeBytes()) {
                        EntityUtils.consumeQuietly(entity);
                        return AcquisitionResult.failure(
                                AcquisitionOutcome.RESPONSE_TOO_LARGE,
                                requestedUrl,
                                currentUrl,
                                "Content-Length " + entity.getContentLength() + " exceeds maximum allowed size of " + config.maxResponseSizeBytes() + " bytes",
                                redirectChain
                        );
                    }

                    byte[] bodyBytes = new byte[0];
                    if (entity != null) {
                        try (InputStream is = entity.getContent()) {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            byte[] buffer = new byte[8192];
                            long totalBytesRead = 0;
                            int bytesRead;
                            boolean sizeExceeded = false;

                            while ((bytesRead = is.read(buffer)) != -1) {
                                totalBytesRead += bytesRead;
                                if (totalBytesRead > config.maxResponseSizeBytes()) {
                                    sizeExceeded = true;
                                    break;
                                }
                                baos.write(buffer, 0, bytesRead);
                            }

                            if (sizeExceeded) {
                                EntityUtils.consumeQuietly(entity);
                                return AcquisitionResult.failure(
                                        AcquisitionOutcome.RESPONSE_TOO_LARGE,
                                        requestedUrl,
                                        currentUrl,
                                        "Response body size exceeded maximum allowed limit of " + config.maxResponseSizeBytes() + " bytes",
                                        redirectChain
                                );
                            }
                            bodyBytes = baos.toByteArray();
                        }
                    }

                    Charset charset = parseCharset(contentType);
                    String bodyString = new String(bodyBytes, charset);

                    Map<String, String> provenanceMetadata = buildProvenanceMetadata(
                            requestedUrl,
                            currentUrl,
                            statusCode,
                            contentType,
                            retrievalTimestamp,
                            redirectCount,
                            response
                    );

                    AcquisitionOutcome outcome = (statusCode >= 200 && statusCode < 300)
                            ? AcquisitionOutcome.SUCCESS
                            : AcquisitionOutcome.HTTP_ERROR;

                    String errorMessage = (outcome == AcquisitionOutcome.HTTP_ERROR)
                            ? "HTTP request failed with status " + statusCode
                            : null;

                    return new AcquisitionResult(
                            outcome,
                            requestedUrl,
                            currentUrl,
                            statusCode,
                            contentType,
                            retrievalTimestamp,
                            AcquisitionMethod.HTTP_PUBLIC,
                            bodyString,
                            provenanceMetadata,
                            redirectChain,
                            errorMessage
                    );
                } catch (UnknownHostException e) {
                    String msg = e.getMessage() != null ? e.getMessage() : e.toString();
                    if (msg.contains("non-public")) {
                        return AcquisitionResult.failure(
                                AcquisitionOutcome.INVALID_TARGET,
                                requestedUrl,
                                currentUrl,
                                msg,
                                redirectChain
                        );
                    }
                    return AcquisitionResult.failure(
                            AcquisitionOutcome.NETWORK_ERROR,
                            requestedUrl,
                            currentUrl,
                            "Network error - Unknown host: " + msg,
                            redirectChain
                    );
                } catch (SSLException e) {
                    return AcquisitionResult.failure(
                            AcquisitionOutcome.NETWORK_ERROR,
                            requestedUrl,
                            currentUrl,
                            "SSL/TLS Handshake failed: " + e.getMessage(),
                            redirectChain
                    );
                } catch (ConnectException e) {
                    return AcquisitionResult.failure(
                            AcquisitionOutcome.NETWORK_ERROR,
                            requestedUrl,
                            currentUrl,
                            "Network error - Connection failed: " + e.getMessage(),
                            redirectChain
                    );
                } catch (IOException e) {
                    String msg = e.getMessage() != null ? e.getMessage() : e.toString();
                    String lower = msg.toLowerCase();
                    if (lower.contains("timeout") || lower.contains("timed out")) {
                        return AcquisitionResult.failure(
                                AcquisitionOutcome.TIMEOUT,
                                requestedUrl,
                                currentUrl,
                                "Read/connect timeout of " + config.readTimeout() + " exceeded: " + msg,
                                redirectChain
                        );
                    }
                    return AcquisitionResult.failure(
                            AcquisitionOutcome.NETWORK_ERROR,
                            requestedUrl,
                            currentUrl,
                            "Network error: " + msg,
                            redirectChain
                    );
                }
            }
        } finally {
            if (!isCustomClient && clientToUse != null) {
                try {
                    clientToUse.close();
                } catch (IOException ignored) {}
            }
        }
    }

    private CloseableHttpClient createHttpClient(AcquisitionConfig config) {
        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setDnsResolver(new PublicWebDnsResolver(config.allowLocalhost()))
                .build();

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(config.connectTimeout().toMillis()))
                .setResponseTimeout(Timeout.ofMilliseconds(config.readTimeout().toMillis()))
                .setRedirectsEnabled(false)
                .build();

        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .setUserAgent(config.userAgent())
                .disableRedirectHandling()
                .build();
    }

    private Map<String, String> buildProvenanceMetadata(
            String requestedUrl,
            String finalUrl,
            int statusCode,
            String contentType,
            Instant retrievalTimestamp,
            int redirectCount,
            CloseableHttpResponse response
    ) {
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("requestedUrl", requestedUrl);
        metadata.put("finalUrl", finalUrl);
        metadata.put("httpStatus", String.valueOf(statusCode));
        metadata.put("contentType", contentType != null ? contentType : "unknown");
        metadata.put("retrievalTimestamp", retrievalTimestamp.toString());
        metadata.put("acquisitionMethod", AcquisitionMethod.HTTP_PUBLIC.name());
        metadata.put("redirectCount", String.valueOf(redirectCount));

        Header serverHeader = response.getFirstHeader("Server");
        if (serverHeader != null) metadata.put("header.server", serverHeader.getValue());

        Header etagHeader = response.getFirstHeader("ETag");
        if (etagHeader != null) metadata.put("header.etag", etagHeader.getValue());

        Header lastModHeader = response.getFirstHeader("Last-Modified");
        if (lastModHeader != null) metadata.put("header.last-modified", lastModHeader.getValue());

        Header cacheHeader = response.getFirstHeader("Cache-Control");
        if (cacheHeader != null) metadata.put("header.cache-control", cacheHeader.getValue());

        return Collections.unmodifiableMap(metadata);
    }

    private Charset parseCharset(String contentType) {
        if (contentType == null) {
            return StandardCharsets.UTF_8;
        }
        String lower = contentType.toLowerCase();
        int charsetIndex = lower.indexOf("charset=");
        if (charsetIndex != -1) {
            String charsetName = contentType.substring(charsetIndex + 8).trim();
            int semicolon = charsetName.indexOf(';');
            if (semicolon != -1) {
                charsetName = charsetName.substring(0, semicolon).trim();
            }
            try {
                return Charset.forName(charsetName);
            } catch (Exception ignored) {}
        }
        return StandardCharsets.UTF_8;
    }
}
