package com.antenapro.hotelcheck.sitecheck;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class IpHasherTest {

    @Test
    void hash_ShouldBeStableWithinADay_DifferAcrossDays_AndNeverContainTheAddress() {
        Clock day1 = Clock.fixed(Instant.parse("2026-10-24T10:00:00Z"), ZoneOffset.UTC);
        Clock day2 = Clock.fixed(Instant.parse("2026-10-25T10:00:00Z"), ZoneOffset.UTC);
        IpHasher a = new IpHasher(day1, "secret");
        IpHasher b = new IpHasher(day2, "secret");

        String h1 = a.hash("203.0.113.7");
        assertThat(h1).hasSize(64).doesNotContain("203.0.113").isEqualTo(a.hash("203.0.113.7"));
        assertThat(b.hash("203.0.113.7")).isNotEqualTo(h1);
        assertThat(a.hash("203.0.113.8")).isNotEqualTo(h1);
        assertThat(new IpHasher(day1, "other").hash("203.0.113.7")).isNotEqualTo(h1);
        assertThat(a.hash(null)).hasSize(64);
    }
}
