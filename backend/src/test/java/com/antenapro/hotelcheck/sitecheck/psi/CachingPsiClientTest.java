package com.antenapro.hotelcheck.sitecheck.psi;

import com.antenapro.hotelcheck.sitecheck.SiteCheckProperties;
import com.antenapro.hotelcheck.sitecheck.Snapshots;
import com.antenapro.hotelcheck.sitecheck.model.PsiResult;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class CachingPsiClientTest {

    private static final PsiResult OK = new PsiResult(true, null, true, true, 1900, 0.03, 120, "field", 1_000_000L, 20, 90);
    private Instant now = Instant.parse("2026-10-24T10:00:00Z");
    private final Clock clock = new Clock() {
        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    };

    @Test
    void analyze_ShouldCallOncePerHostPer24Hours_IgnoringWwwAndPath() {
        Snapshots.FakePsiClient delegate = new Snapshots.FakePsiClient(OK);
        CachingPsiClient client = new CachingPsiClient(delegate, clock);

        client.analyze(URI.create("https://hotel.example.com/"));
        client.analyze(URI.create("https://www.hotel.example.com/rooms"));
        assertThat(delegate.calls).isEqualTo(1);

        now = now.plus(Duration.ofHours(25));
        client.analyze(URI.create("https://hotel.example.com/"));
        assertThat(delegate.calls).isEqualTo(2);
    }

    @Test
    void analyze_ShouldNotCacheAFailure() {
        Snapshots.FakePsiClient delegate = new Snapshots.FakePsiClient(PsiResult.unavailable("quota"));
        CachingPsiClient client = new CachingPsiClient(delegate, clock);

        client.analyze(URI.create("https://hotel.example.com/"));
        client.analyze(URI.create("https://hotel.example.com/"));
        assertThat(delegate.calls).isEqualTo(2);
    }

    @Test
    void httpClient_ShouldNotCallGoogleWithoutAKey() {
        PsiResult r = new HttpPsiClient(SiteCheckProperties.defaults(), "http://127.0.0.1:9/never").analyze(URI.create("https://hotel.example.com/"));

        assertThat(r.available()).isFalse();
        assertThat(r.limitation()).isEqualTo(HttpPsiClient.NO_KEY_LIMITATION);
    }
}
