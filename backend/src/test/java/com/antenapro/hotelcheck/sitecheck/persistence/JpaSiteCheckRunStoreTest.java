package com.antenapro.hotelcheck.sitecheck.persistence;

import com.antenapro.hotelcheck.sitecheck.SiteAnalyzer;
import com.antenapro.hotelcheck.sitecheck.Snapshots;
import com.antenapro.hotelcheck.sitecheck.check.SiteChecks;
import com.antenapro.hotelcheck.sitecheck.model.SiteCheckRun;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** Against the embedded H2 database (the production datastore is PostgreSQL per ADR-001; the column types are portable). */
@DataJpaTest
class JpaSiteCheckRunStoreTest {

    @Autowired
    private SpringDataSiteCheckRunRepository repository;

    @Test
    void save_ShouldStoreScoresFindingsAndIpHash_NeverThePage_AndRecordConsentLater() {
        JpaSiteCheckRunStore store = new JpaSiteCheckRunStore(repository, new ObjectMapper().findAndRegisterModules());
        SiteCheckRun run = new SiteCheckRun(UUID.randomUUID(), URI.create("https://www.hotel.example.com/rooms"), Instant.parse("2026-10-24T06:00:00Z"));
        run.complete(new SiteAnalyzer(SiteChecks.all()).analyze(Snapshots.home("https://www.hotel.example.com/", Snapshots.fixture("wix-ota-only.html"))), Instant.parse("2026-10-24T06:00:10Z"));

        store.save(run, "abc123");
        SiteCheckRunJpaEntity row = repository.findById(run.id()).orElseThrow();

        assertThat(row.getHost()).isEqualTo("hotel.example.com");
        assertThat(row.getStatus()).isEqualTo("DONE");
        assertThat(row.getScore()).isEqualTo(run.result().score());
        assertThat(row.getFindingsJson()).contains("\"check\":\"booking-cta\"").contains("\"evidence\"").doesNotContain("<!DOCTYPE").doesNotContain("SITE_HEADER").doesNotContain("comp-hero");
        assertThat(row.getStagesJson()).contains("\"stage\":\"DISCOVER\"");
        assertThat(row.getIpHash()).isEqualTo("abc123");
        assertThat(row.getEmail()).isNull();
        assertThat(row.getConsentSource()).isEqualTo("hotel_check_form");
        assertThat(row.getCreatedAt()).isEqualTo(Instant.parse("2026-10-24T06:00:00Z"));

        assertThat(store.recordConsent(run.id(), "owner@example.com", Instant.parse("2026-10-24T06:05:00Z"))).isTrue();
        SiteCheckRunJpaEntity consented = repository.findById(run.id()).orElseThrow();
        assertThat(consented.getEmail()).isEqualTo("owner@example.com");
        assertThat(consented.getConsentAt()).isEqualTo(Instant.parse("2026-10-24T06:05:00Z"));
        assertThat(store.recordConsent(UUID.randomUUID(), "x@example.com", Instant.now())).isFalse();
    }

    @Test
    void save_ShouldStoreAFailedRun_WithItsFailureAsTheLimitation() {
        JpaSiteCheckRunStore store = new JpaSiteCheckRunStore(repository, new ObjectMapper().findAndRegisterModules());
        SiteCheckRun run = new SiteCheckRun(UUID.randomUUID(), URI.create("https://hotel.example.com/"), Instant.parse("2026-10-24T06:00:00Z"));
        run.fail("The analysis hit an internal error.", Instant.parse("2026-10-24T06:00:01Z"));

        store.save(run, "h");
        SiteCheckRunJpaEntity row = repository.findById(run.id()).orElseThrow();

        assertThat(row.getStatus()).isEqualTo("FAILED");
        assertThat(row.getScore()).isNull();
        assertThat(row.getLimitationsJson()).contains("internal error");
    }
}
