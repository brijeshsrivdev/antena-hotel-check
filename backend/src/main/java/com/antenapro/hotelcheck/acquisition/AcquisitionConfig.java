package com.antenapro.hotelcheck.acquisition;

import java.time.Duration;

public record AcquisitionConfig(
        Duration connectTimeout,
        Duration readTimeout,
        long maxResponseSizeBytes,
        int maxRedirects,
        String userAgent,
        boolean allowLocalhost
) {
    public static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(5);
    public static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(10);
    public static final long DEFAULT_MAX_RESPONSE_SIZE_BYTES = 2 * 1024 * 1024; // 2 MB
    public static final int DEFAULT_MAX_REDIRECTS = 5;
    public static final String DEFAULT_USER_AGENT = "AntenaHotelCheck/1.0 (Public Experience Analyzer)";

    public AcquisitionConfig {
        if (connectTimeout == null) connectTimeout = DEFAULT_CONNECT_TIMEOUT;
        if (readTimeout == null) readTimeout = DEFAULT_READ_TIMEOUT;
        if (maxResponseSizeBytes <= 0) maxResponseSizeBytes = DEFAULT_MAX_RESPONSE_SIZE_BYTES;
        if (maxRedirects < 0) maxRedirects = DEFAULT_MAX_REDIRECTS;
        if (userAgent == null || userAgent.isBlank()) userAgent = DEFAULT_USER_AGENT;
    }

    public static AcquisitionConfig defaults() {
        return new AcquisitionConfig(
                DEFAULT_CONNECT_TIMEOUT,
                DEFAULT_READ_TIMEOUT,
                DEFAULT_MAX_RESPONSE_SIZE_BYTES,
                DEFAULT_MAX_REDIRECTS,
                DEFAULT_USER_AGENT,
                false
        );
    }

    public static AcquisitionConfig forTesting(boolean allowLocalhost) {
        return new AcquisitionConfig(
                Duration.ofSeconds(2),
                Duration.ofSeconds(2),
                1024 * 1024,
                5,
                DEFAULT_USER_AGENT,
                allowLocalhost
        );
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Duration connectTimeout = DEFAULT_CONNECT_TIMEOUT;
        private Duration readTimeout = DEFAULT_READ_TIMEOUT;
        private long maxResponseSizeBytes = DEFAULT_MAX_RESPONSE_SIZE_BYTES;
        private int maxRedirects = DEFAULT_MAX_REDIRECTS;
        private String userAgent = DEFAULT_USER_AGENT;
        private boolean allowLocalhost = false;

        public Builder connectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder readTimeout(Duration readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public Builder maxResponseSizeBytes(long maxResponseSizeBytes) {
            this.maxResponseSizeBytes = maxResponseSizeBytes;
            return this;
        }

        public Builder maxRedirects(int maxRedirects) {
            this.maxRedirects = maxRedirects;
            return this;
        }

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder allowLocalhost(boolean allowLocalhost) {
            this.allowLocalhost = allowLocalhost;
            return this;
        }

        public AcquisitionConfig build() {
            return new AcquisitionConfig(
                    connectTimeout,
                    readTimeout,
                    maxResponseSizeBytes,
                    maxRedirects,
                    userAgent,
                    allowLocalhost
            );
        }
    }
}
