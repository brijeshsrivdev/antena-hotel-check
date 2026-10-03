package com.antenapro.hotelcheck.acquisition;

import java.net.Inet6Address;
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
            NormalizationResult ipCheck = validateHostIpAddresses(lowerHost, false);
            if (!ipCheck.valid()) {
                return ipCheck;
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

    public static NormalizationResult validateHostIpAddresses(String host, boolean allowLocalhost) {
        if (allowLocalhost) {
            return NormalizationResult.success(host);
        }
        String lowerHost = host.toLowerCase(Locale.ROOT);
        if ("localhost".equals(lowerHost) || "127.0.0.1".equals(lowerHost) || "0.0.0.0".equals(lowerHost) || "::1".equals(lowerHost)) {
            return NormalizationResult.failure(AcquisitionOutcome.INVALID_TARGET, "Target resolves to non-public loopback network destination: " + host);
        }
        try {
            InetAddress[] addresses = InetAddress.getAllByName(host);
            if (addresses == null || addresses.length == 0) {
                return NormalizationResult.failure(AcquisitionOutcome.INVALID_TARGET, "Target host could not be resolved to any IP address: " + host);
            }
            for (InetAddress addr : addresses) {
                if (!isPublicIpAddress(addr, false)) {
                    return NormalizationResult.failure(
                            AcquisitionOutcome.INVALID_TARGET,
                            "Target host '" + host + "' resolves to non-public network address: " + addr.getHostAddress()
                    );
                }
            }
        } catch (UnknownHostException e) {
            // Resolution failure will be handled during HTTP execution as NETWORK_ERROR
        }
        return NormalizationResult.success(host);
    }

    public static boolean isPublicIpAddress(InetAddress address, boolean allowLocalhost) {
        if (allowLocalhost) {
            return true;
        }
        if (address == null) {
            return false;
        }
        if (address.isLoopbackAddress() || address.isAnyLocalAddress() || address.isSiteLocalAddress() || address.isLinkLocalAddress() || address.isMulticastAddress()) {
            return false;
        }
        byte[] bytes = address.getAddress();
        if (bytes == null) {
            return false;
        }
        if (bytes.length == 4) {
            int b0 = bytes[0] & 0xFF;
            int b1 = bytes[1] & 0xFF;

            if (b0 == 127 || b0 == 10 || b0 == 0) return false; // 127.0.0.0/8, 10.0.0.0/8, 0.0.0.0/8
            if (b0 == 172 && (b1 >= 16 && b1 <= 31)) return false; // 172.16.0.0/12
            if (b0 == 192 && b1 == 168) return false; // 192.168.0.0/16
            if (b0 == 169 && b1 == 254) return false; // 169.254.0.0/16 (Link-Local / AWS IMDS)
            if (b0 == 100 && (b1 >= 64 && b1 <= 127)) return false; // 100.64.0.0/10 (CGNAT)
            if (b0 >= 224) return false; // Multicast / Reserved
        }
        return true;
    }
}
