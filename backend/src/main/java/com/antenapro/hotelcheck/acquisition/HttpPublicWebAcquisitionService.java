package com.antenapro.hotelcheck.acquisition;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class HttpPublicWebAcquisitionService implements PublicWebAcquisitionService {

    static {
        // Enable setting custom Host header when sending requests to IP-pinned URIs
        String existing = System.getProperty("jdk.httpclient.allowRestrictedHeaders");
        if (existing == null || existing.isBlank()) {
            System.setProperty("jdk.httpclient.allowRestrictedHeaders", "host,Host");
        } else if (!existing.toLowerCase().contains("host")) {
            System.setProperty("jdk.httpclient.allowRestrictedHeaders", existing + ",host,Host");
        }
    }

    private final HttpClient httpClient;

    public HttpPublicWebAcquisitionService() {
        this(HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build());
    }

    public HttpPublicWebAcquisitionService(HttpClient httpClient) {
        this.httpClient = httpClient;
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

        HttpClient clientToUse = this.httpClient;
        if (clientToUse.connectTimeout().isEmpty() || !clientToUse.connectTimeout().get().equals(config.connectTimeout())) {
            clientToUse = HttpClient.newBuilder()
                    .connectTimeout(config.connectTimeout())
                    .followRedirects(HttpClient.Redirect.NEVER)
                    .build();
        }

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

            URI currentUri = URI.create(currentNorm.normalizedUrl());
            String host = currentUri.getHost();
            int port = currentUri.getPort();
            String scheme = currentUri.getScheme();

            // Single DNS resolution step: Resolve host to IP addresses once
            InetAddress[] addresses;
            try {
                addresses = InetAddress.getAllByName(host);
            } catch (UnknownHostException e) {
                return AcquisitionResult.failure(
                        AcquisitionOutcome.NETWORK_ERROR,
                        requestedUrl,
                        currentUrl,
                        "Network error - Unknown host: " + e.getMessage(),
                        redirectChain
                );
            }

            // Validate ALL resolved IP addresses against public network boundaries
            if (!config.allowLocalhost()) {
                for (InetAddress addr : addresses) {
                    if (!AcquisitionUrlNormalizer.isPublicIpAddress(addr, false)) {
                        return AcquisitionResult.failure(
                                AcquisitionOutcome.INVALID_TARGET,
                                requestedUrl,
                                currentUrl,
                                "Target host '" + host + "' resolves to non-public network address: " + addr.getHostAddress(),
                                redirectChain
                        );
                    }
                }
            }

            // Pin socket connection directly to the pre-validated IP address to eliminate secondary DNS resolution / TOCTOU rebinding
            InetAddress targetAddress = addresses[0];
            String ipHost = (targetAddress instanceof Inet6Address)
                    ? "[" + targetAddress.getHostAddress() + "]"
                    : targetAddress.getHostAddress();

            URI pinnedUri;
            try {
                pinnedUri = new URI(
                        scheme,
                        currentUri.getRawUserInfo(),
                        ipHost,
                        port,
                        currentUri.getRawPath(),
                        currentUri.getRawQuery(),
                        null
                );
            } catch (Exception e) {
                return AcquisitionResult.failure(
                        AcquisitionOutcome.INVALID_TARGET,
                        requestedUrl,
                        currentUrl,
                        "Failed to construct pinned URI: " + e.getMessage(),
                        redirectChain
                );
            }

            String hostHeaderValue = host + (port != -1 ? ":" + port : "");

            HttpRequest httpRequest;
            try {
                httpRequest = HttpRequest.newBuilder()
                        .uri(pinnedUri)
                        .timeout(config.readTimeout())
                        .header("Host", hostHeaderValue)
                        .header("User-Agent", config.userAgent())
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                        .GET()
                        .build();
            } catch (Exception e) {
                return AcquisitionResult.failure(
                        AcquisitionOutcome.INVALID_TARGET,
                        requestedUrl,
                        currentUrl,
                        "Failed to construct HTTP request: " + e.getMessage(),
                        redirectChain
                );
            }

            Instant retrievalTimestamp = Instant.now();
            HttpResponse<InputStream> response;

            try {
                response = clientToUse.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());
            } catch (HttpTimeoutException e) {
                return AcquisitionResult.failure(
                        AcquisitionOutcome.TIMEOUT,
                        requestedUrl,
                        currentUrl,
                        "Read timeout of " + config.readTimeout() + " exceeded",
                        redirectChain
                );
            } catch (UnknownHostException e) {
                return AcquisitionResult.failure(
                        AcquisitionOutcome.NETWORK_ERROR,
                        requestedUrl,
                        currentUrl,
                        "Network error - Unknown host: " + e.getMessage(),
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
                if (msg.toLowerCase().contains("timeout")) {
                    return AcquisitionResult.failure(
                            AcquisitionOutcome.TIMEOUT,
                            requestedUrl,
                            currentUrl,
                            "Timeout encountered: " + msg,
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
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return AcquisitionResult.failure(
                        AcquisitionOutcome.NETWORK_ERROR,
                        requestedUrl,
                        currentUrl,
                        "Request interrupted",
                        redirectChain
                );
            }

            int statusCode = response.statusCode();
            HttpHeaders headers = response.headers();
            Optional<String> contentType = headers.firstValue("Content-Type");

            // Handle 3xx Redirects
            if (statusCode >= 300 && statusCode < 400) {
                Optional<String> location = headers.firstValue("Location");
                try {
                    response.body().close();
                } catch (IOException ignored) {}

                if (location.isEmpty() || location.get().isBlank()) {
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
                    URI currentUriObj = URI.create(currentUrl);
                    URI nextUri = currentUriObj.resolve(location.get());
                    currentUrl = nextUri.toString();
                } catch (Exception e) {
                    return AcquisitionResult.failure(
                            AcquisitionOutcome.INVALID_TARGET,
                            requestedUrl,
                            currentUrl,
                            "Invalid redirect target location: " + location.get(),
                            redirectChain
                    );
                }
                continue;
            }

            // Check Content-Length if present
            Optional<String> contentLengthOpt = headers.firstValue("Content-Length");
            if (contentLengthOpt.isPresent()) {
                try {
                    long contentLength = Long.parseLong(contentLengthOpt.get().trim());
                    if (contentLength > config.maxResponseSizeBytes()) {
                        try { response.body().close(); } catch (IOException ignored) {}
                        return AcquisitionResult.failure(
                                AcquisitionOutcome.RESPONSE_TOO_LARGE,
                                requestedUrl,
                                currentUrl,
                                "Content-Length " + contentLength + " exceeds maximum allowed size of " + config.maxResponseSizeBytes() + " bytes",
                                redirectChain
                        );
                    }
                } catch (NumberFormatException ignored) {}
            }

            // Read response body with streaming size enforcement
            byte[] bodyBytes;
            try (InputStream is = response.body()) {
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
                    return AcquisitionResult.failure(
                            AcquisitionOutcome.RESPONSE_TOO_LARGE,
                            requestedUrl,
                            currentUrl,
                            "Response body size exceeded maximum allowed limit of " + config.maxResponseSizeBytes() + " bytes",
                            redirectChain
                    );
                }

                bodyBytes = baos.toByteArray();
            } catch (IOException e) {
                return AcquisitionResult.failure(
                        AcquisitionOutcome.NETWORK_ERROR,
                        requestedUrl,
                        currentUrl,
                        "Failed to read response body: " + e.getMessage(),
                        redirectChain
                );
            }

            Charset charset = parseCharset(contentType.orElse(null));
            String bodyString = new String(bodyBytes, charset);

            Map<String, String> provenanceMetadata = buildProvenanceMetadata(
                    requestedUrl,
                    currentUrl,
                    statusCode,
                    contentType.orElse(null),
                    retrievalTimestamp,
                    redirectCount,
                    headers
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
                    contentType.orElse(null),
                    retrievalTimestamp,
                    AcquisitionMethod.HTTP_PUBLIC,
                    bodyString,
                    provenanceMetadata,
                    redirectChain,
                    errorMessage
            );
        }
    }

    private Map<String, String> buildProvenanceMetadata(
            String requestedUrl,
            String finalUrl,
            int statusCode,
            String contentType,
            Instant retrievalTimestamp,
            int redirectCount,
            HttpHeaders headers
    ) {
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("requestedUrl", requestedUrl);
        metadata.put("finalUrl", finalUrl);
        metadata.put("httpStatus", String.valueOf(statusCode));
        metadata.put("contentType", contentType != null ? contentType : "unknown");
        metadata.put("retrievalTimestamp", retrievalTimestamp.toString());
        metadata.put("acquisitionMethod", AcquisitionMethod.HTTP_PUBLIC.name());
        metadata.put("redirectCount", String.valueOf(redirectCount));

        headers.firstValue("Server").ifPresent(v -> metadata.put("header.server", v));
        headers.firstValue("ETag").ifPresent(v -> metadata.put("header.etag", v));
        headers.firstValue("Last-Modified").ifPresent(v -> metadata.put("header.last-modified", v));
        headers.firstValue("Cache-Control").ifPresent(v -> metadata.put("header.cache-control", v));

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
