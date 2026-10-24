package com.antenapro.hotelcheck.sitecheck.model;

/**
 * Mean of a stage's known checks (PASS 1, PARTIAL 0.5, FAIL 0) as 0–100, or {@code null} when every check
 * in the stage is UNKNOWN.
 */
public record StageScore(Stage stage, Integer score, int pass, int partial, int fail, int unknown) {
}
