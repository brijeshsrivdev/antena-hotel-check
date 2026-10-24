package com.antenapro.hotelcheck.sitecheck.report;

import com.antenapro.hotelcheck.sitecheck.SiteAnalyzer;
import com.antenapro.hotelcheck.sitecheck.Snapshots;
import com.antenapro.hotelcheck.sitecheck.api.SiteCheckResponse;
import com.antenapro.hotelcheck.sitecheck.check.SiteChecks;
import com.antenapro.hotelcheck.sitecheck.model.SiteCheckRun;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PdfReportRendererTest {

    private static SiteCheckRun finishedRun(String url, String html) {
        SiteCheckRun run = new SiteCheckRun(UUID.randomUUID(), URI.create(url), Instant.parse("2026-10-24T06:00:00Z"));
        run.complete(new SiteAnalyzer(SiteChecks.all()).analyze(Snapshots.home(url, html)), Instant.parse("2026-10-24T06:00:12Z"));
        return run;
    }

    @Test
    void render_ShouldProduceAPdf_WithTheFindingsOfTheRun() {
        SiteCheckRun run = finishedRun("https://www.seashellresortdiveagar.com/", Snapshots.fixture("wix-ota-only.html"));

        byte[] pdf = new PdfReportRenderer().render(SiteCheckResponse.of(run));

        assertThat(new String(pdf, 0, 5, StandardCharsets.ISO_8859_1)).isEqualTo("%PDF-");
        assertThat(pdf.length).isGreaterThan(5_000);
    }

    @Test
    void html_ShouldEscapeEvidence_AndListFindingsFixFirst() {
        SiteCheckRun run = finishedRun("https://hotel.example.com/", "<html><head><title>x <script>alert(1)</script></title></head><body>&</body></html>");

        String html = ReportHtml.render(SiteCheckResponse.of(run));

        assertThat(html).doesNotContain("<script>").contains("&lt;script&gt;");
        assertThat(html).contains("Hotel Site Check").contains("hotel.example.com").contains("FIX THIS").contains("Pages read");
        assertThat(html.indexOf("<span class=\"tag FAIL\">")).isGreaterThan(0).isLessThan(html.indexOf("<span class=\"tag UNKNOWN\">"));
        assertThat(ReportHtml.esc("a & b < c \"d\" \u0001")).isEqualTo("a &amp; b &lt; c &quot;d&quot; ");
    }
}
