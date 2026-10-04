package com.antenapro.hotelcheck.analysis;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Immutable aggregation boundary for the hospitality analysis artifacts of one evaluation.
 *
 * <p>This type preserves findings, limitations, and coverage as supplied. It does not
 * classify, score, recommend, or reinterpret any of those artifacts.</p>
 */
public record HospitalityAnalysisResult(
        UUID evaluationId,
        Set<HospitalityFinding> findings,
        Set<HospitalityAnalysisLimitation> limitations,
        HospitalityAnalysisCoverage coverage
) {
    public HospitalityAnalysisResult {
        Objects.requireNonNull(evaluationId, "evaluationId must not be null");
        Objects.requireNonNull(findings, "findings must not be null");
        Objects.requireNonNull(limitations, "limitations must not be null");
        Objects.requireNonNull(coverage, "coverage must not be null");

        if (!evaluationId.equals(coverage.evaluationId())) {
            throw new IllegalArgumentException("coverage must belong to evaluationId");
        }
        if (findings.stream().anyMatch(finding -> !evaluationId.equals(finding.evaluationId()))) {
            throw new IllegalArgumentException("findings must belong to evaluationId");
        }
        if (limitations.stream().anyMatch(limitation -> !evaluationId.equals(limitation.evaluationId()))) {
            throw new IllegalArgumentException("limitations must belong to evaluationId");
        }

        findings = Set.copyOf(findings);
        limitations = Set.copyOf(limitations);
    }
}
