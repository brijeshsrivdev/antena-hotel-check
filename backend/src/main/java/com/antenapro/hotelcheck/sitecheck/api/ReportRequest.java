package com.antenapro.hotelcheck.sitecheck.api;

/** Body of {@code POST /api/site-checks/{runId}/report}; {@code consent} must be literally {@code true}. */
public record ReportRequest(String email, Boolean consent) {
}
