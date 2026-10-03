package com.antenapro.hotelcheck.acquisition;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Locale;

public class AcquisitionUrlNormalizer {

    public record NormalizationResult(
            boolean valid,
            String normalizedUrl,
            AcquisitionOutcome outcome,
            String errorMessage
    ) {
        public static NormalizationResult success(String normalizedUrl) {
            return new NormalizationResult(true, normalizedUrl, AcquisitionOutcome.SUCCESS, null);
        }

        public static NormalizationResult failure(AcquisitionOutcome outcome, String errorMessage) {
            return new NormalizationResult(false, null, outcome, errorMessage);
        }
    }

    public static NormalizationResult normalizeAndValidate(String targetUrl, boolean allowLocalhost) {
        if (targetUrl == null || targetUrl.isBlank()) {
            return NormalizationResult.failure(AcquisitionOutcome.INVALID_TARGET, "Target URL is null or empty");
        }

        String trimmed = targetUrl.trim();
        URI uri;
        try {
            uri = new URI(trimmed);
        } catch (URISyntaxException e) {
            return NormalizationResult.failure(AcquisitionOutcome.INVALID_TARGET, "Malformed target URL syntax: " + e.getMessage());
        }

        String scheme = uri.getScheme();
        if (scheme == null) {
            return NormalizationResult.failure(AcquisitionOutcome.UNSUPPORTED_SCHEME, "Missing URI scheme in target URL");
        }

        String lowerScheme = scheme.toLowerCase(Locale.ROOT);
        if (!"http".equals(lowerScheme) && !"https".equals(lowerScheme)) {
            return NormalizationResult.failure(AcquisitionOutcome.UNSUPPORTED_SCHEME, "Unsupported scheme '" + scheme + "'. Only HTTP and HTTPS are permitted.");
        }

        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            return NormalizationResult.failure(AcquisitionOutcome.INVALID_TARGET, "Target URL lacks a valid host name");
        }

        String lowerHost = host.toLowerCase(Locale.ROOT);

        if (!allowLocalhost) {
            if ("localhost".equals(lowerHost) || "127.0.0.1".equals(lowerHost) || "0.0.0.0".equals(lowerHost) || "::1".equals(lowerHost)) {
                return NormalizationResult.failure(AcquisitionOutcome.INVALID_TARGET, "Target resolves to non-public loopback network destination");
            }
            try {
                InetAddress address = InetAddress.getByName(host);
                if (address.isLoopbackAddress() || address.isAnyLocalAddress() || address.isSiteLocalAddress() || address.isLinkLocalAddress()) {
                    return NormalizationResult.failure(AcquisitionOutcome.INVALID_TARGET, "Target resolves to non-public / internal network destination");
                }
            } catch (UnknownHostException e) {
                // Resolution failures will be handled during actual HTTP request as NETWORK_ERROR
            }
        }

        // Reconstruct canonical normalized URI
        try {
            int port = uri.getPort();
            if (("http".equals(lowerScheme) && port == 80) || ("https".equals(lowerScheme) && port == 443)) {
                port = -1; // Strip default ports
            }

            String path = uri.getRawPath();
            if (path == null || path.isEmpty()) {
                path = "/";
            }

            URI normalizedUri = new URI(
                    lowerScheme,
                    uri.getRawUserInfo(),
                    lowerHost,
                    port,
                    path,
                    uri.getRawQuery(),
                    null // Strip fragments (#...)
            );

            return NormalizationResult.success(normalizedUri.toASCIIString());
        } catch (URISyntaxException e) {
            return NormalizationResult.failure(AcquisitionOutcome.INVALID_TARGET, "Failed to normalize target URI: " + e.getMessage());
        }
    }
}
