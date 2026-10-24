package com.antenapro.hotelcheck.sitecheck.model;

/** Outcome of one check. UNKNOWN means we could not observe the signal; it is excluded from the score. */
public enum CheckStatus {
    PASS, PARTIAL, FAIL, UNKNOWN;

    /** Report ordering: what the owner must act on first. */
    public int reportOrder() {
        return switch (this) {
            case FAIL -> 0;
            case PARTIAL -> 1;
            case UNKNOWN -> 2;
            case PASS -> 3;
        };
    }
}
