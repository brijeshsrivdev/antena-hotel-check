package com.antenapro.hotelcheck.analysis;

/**
 * Qualitative coverage states defined by the hospitality analysis contract.
 *
 * <p>The state is an explicit, already-governed classification supplied to
 * {@link HospitalityAnalysisCoverage}. REQ-024 intentionally does not derive
 * or validate the semantic classification from scope, finding count, page
 * count, percentages, or numerical thresholds. Classification rules belong to
 * a separately governed future capability.</p>
 */
public enum HospitalityAnalysisCoverageState {
    SUBSTANTIALLY_ASSESSED,
    PARTIALLY_ASSESSED,
    INSUFFICIENT_COVERAGE
}
