package com.antenapro.hotelcheck.sitecheck.api;

import com.antenapro.hotelcheck.sitecheck.HostLimitExceededException;
import com.antenapro.hotelcheck.sitecheck.IpHasher;
import com.antenapro.hotelcheck.sitecheck.SiteCheckService;
import com.antenapro.hotelcheck.sitecheck.fetch.InvalidUrlException;
import com.antenapro.hotelcheck.sitecheck.model.SiteCheckRun;
import com.antenapro.hotelcheck.sitecheck.report.ReportRequestRejectedException;
import com.antenapro.hotelcheck.sitecheck.report.ReportService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/**
 * Thin HTTP boundary for the Hotel Site Check. The bean exists only with {@code hotelcheck.sitecheck.enabled=true},
 * so the path is a 404 everywhere else. Validation and run semantics stay in {@link SiteCheckService}.
 */
@RestController
@RequestMapping("/api/site-checks")
@ConditionalOnProperty(name = "hotelcheck.sitecheck.enabled", havingValue = "true")
public final class SiteCheckController {

    private final SiteCheckService service;
    private final ReportService reports;
    private final IpHasher ipHasher;

    public SiteCheckController(SiteCheckService service, ReportService reports, IpHasher ipHasher) {
        this.service = service;
        this.reports = reports;
        this.ipHasher = ipHasher;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> start(@RequestBody(required = false) SiteCheckRequest request, HttpServletRequest http) {
        SiteCheckRun run = service.start(request == null ? null : request.url(), ipHasher.hash(http.getRemoteAddr()));
        return ResponseEntity.accepted().body(Map.of("runId", run.id().toString()));
    }

    /** "Email me the PDF": 202 once the report is on its way; 400 without literal consent or a usable address. */
    @PostMapping(value = "/{runId}/report", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> report(@PathVariable String runId, @RequestBody(required = false) ReportRequest request) {
        UUID id = parse(runId);
        if (id == null) {
            return ResponseEntity.notFound().build();
        }
        String note = reports.email(id, request == null ? null : request.email(), request == null ? null : request.consent());
        return ResponseEntity.accepted().body(Map.of("status", "queued", "note", note));
    }

    /** The same PDF as a download, for the developer's machine. */
    @GetMapping(value = "/{runId}/report.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> pdf(@PathVariable String runId) {
        UUID id = parse(runId);
        if (id == null) {
            return ResponseEntity.notFound().build();
        }
        byte[] pdf = reports.pdf(id);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"hotel-site-check-" + id + ".pdf\"").body(pdf);
    }

    @GetMapping(value = "/{runId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SiteCheckResponse> get(@PathVariable String runId) {
        UUID id = parse(runId);
        if (id == null) {
            return ResponseEntity.notFound().build();
        }
        return service.get(id).map(run -> ResponseEntity.ok(SiteCheckResponse.of(run)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private static UUID parse(String runId) {
        try {
            return UUID.fromString(runId);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @ExceptionHandler(InvalidUrlException.class)
    ResponseEntity<Map<String, String>> invalidUrl(InvalidUrlException e) {
        return ResponseEntity.badRequest().body(Map.of("code", "INVALID_SITE_CHECK_URL", "error", e.getMessage()));
    }

    @ExceptionHandler(ReportRequestRejectedException.class)
    ResponseEntity<Map<String, String>> reportRejected(ReportRequestRejectedException e) {
        return ResponseEntity.status(e.httpStatus()).body(Map.of("code", "SITE_CHECK_REPORT_REJECTED", "error", e.getMessage()));
    }

    @ExceptionHandler(HostLimitExceededException.class)
    ResponseEntity<Map<String, String>> hostLimit(HostLimitExceededException e) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of("code", "SITE_CHECK_HOST_LIMIT", "error", e.getMessage()));
    }
}
