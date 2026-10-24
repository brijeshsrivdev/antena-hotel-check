package com.antenapro.hotelcheck.sitecheck.report;

import com.antenapro.hotelcheck.sitecheck.SiteAnalyzer;
import com.antenapro.hotelcheck.sitecheck.SiteCheckRunStore;
import com.antenapro.hotelcheck.sitecheck.SiteCheckService;
import com.antenapro.hotelcheck.sitecheck.Snapshots;
import com.antenapro.hotelcheck.sitecheck.check.SiteChecks;
import com.antenapro.hotelcheck.sitecheck.model.SiteCheckRun;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReportServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-24T10:00:00Z"), ZoneOffset.UTC);

    private final SiteCheckService runs = mock(SiteCheckService.class);
    private final List<String> consents = new ArrayList<>();
    private final List<String> sent = new ArrayList<>();
    private final SiteCheckRunStore store = new SiteCheckRunStore() {
        @Override
        public void save(SiteCheckRun run, String ipHash) {
        }

        @Override
        public boolean recordConsent(UUID runId, String email, Instant consentAt) {
            consents.add(runId + "|" + email + "|" + consentAt);
            return true;
        }
    };
    private final ReportService service = new ReportService(runs, store, new PdfReportRenderer(), (email, host, pdf) -> {
        sent.add(email + "|" + host + "|" + pdf.length);
        return "sent";
    }, CLOCK);

    private SiteCheckRun done() {
        SiteCheckRun run = new SiteCheckRun(UUID.randomUUID(), URI.create("https://hotel.example.com/"), CLOCK.instant());
        run.complete(new SiteAnalyzer(SiteChecks.all()).analyze(Snapshots.home("https://hotel.example.com/", Snapshots.fixture("wordpress-booking.html"))), CLOCK.instant());
        when(runs.get(run.id())).thenReturn(Optional.of(run));
        return run;
    }

    @Test
    void email_ShouldRecordConsentThenSend_ForAFinishedRun() {
        SiteCheckRun run = done();

        String note = service.email(run.id(), " Owner@Example.com ", true);

        assertThat(note).isEqualTo("sent");
        assertThat(consents).singleElement().asString().isEqualTo(run.id() + "|owner@example.com|2026-10-24T10:00:00Z");
        assertThat(sent).singleElement().asString().startsWith("Owner@Example.com|hotel.example.com|");
    }

    @Test
    void email_ShouldRejectWithoutLiteralConsent_OrWithABadAddress_WithoutSendingAnything() {
        SiteCheckRun run = done();

        assertThatThrownBy(() -> service.email(run.id(), "owner@example.com", null)).isInstanceOf(ReportRequestRejectedException.class)
                .extracting("httpStatus").isEqualTo(400);
        assertThatThrownBy(() -> service.email(run.id(), "owner@example.com", false)).hasMessageContaining("Tick the box");
        assertThatThrownBy(() -> service.email(run.id(), "not an email", true)).hasMessageContaining("email address");
        assertThat(consents).isEmpty();
        assertThat(sent).isEmpty();
    }

    @Test
    void email_ShouldAnswer404ForUnknownRuns_And409ForRunsStillRunningOrFailed() {
        when(runs.get(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.email(UUID.randomUUID(), "owner@example.com", true)).extracting("httpStatus").isEqualTo(404);

        SiteCheckRun running = new SiteCheckRun(UUID.randomUUID(), URI.create("https://hotel.example.com/"), CLOCK.instant());
        when(runs.get(running.id())).thenReturn(Optional.of(running));
        assertThatThrownBy(() -> service.email(running.id(), "owner@example.com", true)).extracting("httpStatus").isEqualTo(409);

        SiteCheckRun failed = new SiteCheckRun(UUID.randomUUID(), URI.create("https://hotel.example.com/"), CLOCK.instant());
        failed.fail("boom", CLOCK.instant());
        when(runs.get(failed.id())).thenReturn(Optional.of(failed));
        assertThatThrownBy(() -> service.pdf(failed.id())).extracting("httpStatus").isEqualTo(409);
        assertThat(sent).isEmpty();
    }

    @Test
    void pdf_ShouldRenderTheFinishedRun() {
        SiteCheckRun run = done();

        byte[] pdf = service.pdf(run.id());

        assertThat(new String(pdf, 0, 5, java.nio.charset.StandardCharsets.ISO_8859_1)).isEqualTo("%PDF-");
    }

    @Test
    void fileSender_ShouldWriteThePdf_AndSayWhere() throws Exception {
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("hotelcheck-report-test");
        String note = new FileReportSender(dir).send("owner@example.com", "hotel.example.com", "%PDF-fake".getBytes());

        assertThat(note).contains(dir.toString()).contains("email delivery is off");
        try (var files = java.nio.file.Files.list(dir)) {
            assertThat(files.map(p -> p.getFileName().toString()).toList()).singleElement().asString().startsWith("hotel.example.com-").endsWith(".pdf");
        }
    }
}
