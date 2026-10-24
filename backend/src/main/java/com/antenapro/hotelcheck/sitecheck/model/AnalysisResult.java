package com.antenapro.hotelcheck.sitecheck.model;

import java.util.List;
import java.util.Map;

/**
 * Output of one analysis: findings FAIL-first, stage scores, overall 0–100 score ({@code null} when no
 * check could be observed), the fetch log and limitations.
 */
public record AnalysisResult(Integer score, Map<Stage, StageScore> stages, List<Finding> findings,
                             List<FetchedPage> fetched, List<String> limitations, String finalUrl) {
}
