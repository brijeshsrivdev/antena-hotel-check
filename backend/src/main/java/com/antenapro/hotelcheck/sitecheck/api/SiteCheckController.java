package com.antenapro.hotelcheck.sitecheck.api;

import com.antenapro.hotelcheck.sitecheck.HostLimitExceededException;
import com.antenapro.hotelcheck.sitecheck.SiteCheckService;
import com.antenapro.hotelcheck.sitecheck.fetch.InvalidUrlException;
import com.antenapro.hotelcheck.sitecheck.model.SiteCheckRun;
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

    public SiteCheckController(SiteCheckService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> start(@RequestBody(required = false) SiteCheckRequest request) {
        SiteCheckRun run = service.start(request == null ? null : request.url());
        return ResponseEntity.accepted().body(Map.of("runId", run.id().toString()));
    }

    @GetMapping(value = "/{runId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SiteCheckResponse> get(@PathVariable String runId) {
        UUID id;
        try {
            id = UUID.fromString(runId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
        return service.get(id).map(run -> ResponseEntity.ok(SiteCheckResponse.of(run)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @ExceptionHandler(InvalidUrlException.class)
    ResponseEntity<Map<String, String>> invalidUrl(InvalidUrlException e) {
        return ResponseEntity.badRequest().body(Map.of("code", "INVALID_SITE_CHECK_URL", "error", e.getMessage()));
    }

    @ExceptionHandler(HostLimitExceededException.class)
    ResponseEntity<Map<String, String>> hostLimit(HostLimitExceededException e) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of("code", "SITE_CHECK_HOST_LIMIT", "error", e.getMessage()));
    }
}
