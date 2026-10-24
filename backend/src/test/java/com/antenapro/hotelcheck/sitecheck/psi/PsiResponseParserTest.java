package com.antenapro.hotelcheck.sitecheck.psi;

import com.antenapro.hotelcheck.sitecheck.Snapshots;
import com.antenapro.hotelcheck.sitecheck.model.PsiResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PsiResponseParserTest {

    @Test
    void parse_ShouldPreferFieldData_AndReadAuditsAndRequestCount() {
        PsiResult r = PsiResponseParser.parse(Snapshots.fixture("psi/mobile-success.json"));

        assertThat(r.available()).isTrue();
        assertThat(r.source()).isEqualTo("field");
        assertThat(r.lcpMs()).isEqualTo(1900);
        assertThat(r.cls()).isEqualTo(0.03);
        assertThat(r.inpMs()).isEqualTo(120);
        assertThat(r.viewportPass()).isTrue();
        assertThat(r.contentWidthPass()).isTrue();
        assertThat(r.totalBytes()).isEqualTo(1468006L);
        assertThat(r.requestCount()).isEqualTo(10);
        assertThat(r.performanceScore()).isEqualTo(91);
    }

    @Test
    void parse_ShouldFallBackToLabData_WhenThereIsNoFieldData() {
        PsiResult r = PsiResponseParser.parse(Snapshots.fixture("psi/mobile-poor-lab-only.json"));

        assertThat(r.available()).isTrue();
        assertThat(r.source()).isEqualTo("lab");
        assertThat(r.lcpMs()).isEqualTo(6800);
        assertThat(r.cls()).isEqualTo(0.42);
        assertThat(r.inpMs()).isNull();
        assertThat(r.viewportPass()).isFalse();
        assertThat(r.totalBytes()).isEqualTo(9100000L);
        assertThat(r.requestCount()).isEqualTo(3);
        assertThat(r.performanceScore()).isEqualTo(23);
    }

    @Test
    void parse_ShouldReportQuotaErrors_AsUnavailableWithAReason() {
        PsiResult r = PsiResponseParser.parse(Snapshots.fixture("psi/quota-exceeded.json"));

        assertThat(r.available()).isFalse();
        assertThat(r.limitation()).contains("quota exceeded");
    }

    @Test
    void parse_ShouldReportOtherErrorsNonJsonAndEmptyAudits_AsUnavailable() {
        assertThat(PsiResponseParser.parse("{\"error\":{\"code\":400,\"message\":\"API key not valid. Please pass a valid API key.\"}}").limitation())
                .contains("rejected the request (400)").contains("API key not valid");
        assertThat(PsiResponseParser.parse("<html>502</html>").limitation()).contains("not JSON");
        assertThat(PsiResponseParser.parse("{\"lighthouseResult\":{\"audits\":{}}}").limitation()).contains("without the audits");
    }
}
