package com.antenapro.hotelcheck.sitecheck.report;

import com.antenapro.hotelcheck.sitecheck.SiteCheckRunStore;
import com.antenapro.hotelcheck.sitecheck.SiteCheckService;
import com.antenapro.hotelcheck.sitecheck.api.SiteCheckResponse;
import com.antenapro.hotelcheck.sitecheck.model.RunStatus;
import com.antenapro.hotelcheck.sitecheck.model.SiteCheckRun;
import com.antenapro.hotelcheck.sitecheck.model.SiteSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * "Email me the PDF" (plan §4): only for a finished run, only to the address the visitor typed, only when consent
 * is literally {@code true}. The consent is recorded against the stored run with source {@code hotel_check_form}
 * before the report is sent.
 */
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]{1,64}@[^\\s@]{1,255}\\.[A-Za-z]{2,24}$");

    private final SiteCheckService runs;
    private final SiteCheckRunStore store;
    private final PdfReportRenderer renderer;
    private final ReportSender sender;
    private final Clock clock;

    public ReportService(SiteCheckService runs, SiteCheckRunStore store, PdfReportRenderer renderer, ReportSender sender, Clock clock) {
        this.runs = runs;
        this.store = store;
        this.renderer = renderer;
        this.sender = sender;
        this.clock = clock;
    }

    /** The PDF of a finished run, for a direct download on the developer's machine. */
    public byte[] pdf(UUID runId) {
        return renderer.render(SiteCheckResponse.of(finishedRun(runId)));
    }

    /** Validates, records consent, renders and sends. */
    public String email(UUID runId, String email, Boolean consent) {
        if (!Boolean.TRUE.equals(consent)) {
            throw new ReportRequestRejectedException(400, "Tick the box to confirm you want the report by email and agree that we may follow up.");
        }
        String address = email == null ? "" : email.trim();
        if (!EMAIL.matcher(address).matches()) {
            throw new ReportRequestRejectedException(400, "Enter the email address the report should go to.");
        }
        SiteCheckRun run = finishedRun(runId);
        String host = SiteSnapshot.hostOf(run.requestedUrl().toString());
        boolean recorded = store.recordConsent(run.id(), address.toLowerCase(Locale.ROOT), clock.instant());
        if (!recorded) {
            log.warn("Site check run {} is not in the store; consent for the report could not be recorded", run.id());
        }
        byte[] pdf = renderer.render(SiteCheckResponse.of(run));
        String note = sender.send(address, host, pdf);
        log.info("Site check report for {} requested (run {}, consent recorded: {})", host, run.id(), recorded);
        return note;
    }

    private SiteCheckRun finishedRun(UUID runId) {
        SiteCheckRun run = runs.get(runId).orElseThrow(() -> new ReportRequestRejectedException(404, "This run is no longer available; start a new check."));
        if (run.status() != RunStatus.DONE || run.result() == null) {
            throw new ReportRequestRejectedException(409, run.status() == RunStatus.RUNNING ? "The check is still running." : "The check did not finish, so there is no report.");
        }
        return run;
    }
}
