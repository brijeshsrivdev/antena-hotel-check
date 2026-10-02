package com.antenapro.hotelcheck.evaluation;

public record OwnerFacingOutcome(
        boolean analysisReportAvailable,
        boolean interactivePreviewAvailable
) {
    public boolean complete() {
        return analysisReportAvailable && interactivePreviewAvailable;
    }
}
