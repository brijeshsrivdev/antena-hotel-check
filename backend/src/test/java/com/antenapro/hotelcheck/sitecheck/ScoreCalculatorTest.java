package com.antenapro.hotelcheck.sitecheck;

import com.antenapro.hotelcheck.sitecheck.model.CheckStatus;
import com.antenapro.hotelcheck.sitecheck.model.Finding;
import com.antenapro.hotelcheck.sitecheck.model.Stage;
import com.antenapro.hotelcheck.sitecheck.model.StageScore;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ScoreCalculatorTest {

    private static Finding f(Stage stage, CheckStatus status) {
        return new Finding("c", "t", stage, status, "e", "f", "w", false);
    }

    @Test
    void stageScores_ShouldAverageKnownChecks_AndExcludeUnknown() {
        Map<Stage, StageScore> s = ScoreCalculator.stageScores(List.of(
                f(Stage.DISCOVER, CheckStatus.PASS), f(Stage.DISCOVER, CheckStatus.PARTIAL), f(Stage.DISCOVER, CheckStatus.FAIL), f(Stage.DISCOVER, CheckStatus.UNKNOWN),
                f(Stage.BOOK, CheckStatus.UNKNOWN)));

        assertThat(s.get(Stage.DISCOVER).score()).isEqualTo(50);
        assertThat(s.get(Stage.DISCOVER).unknown()).isEqualTo(1);
        assertThat(s.get(Stage.BOOK).score()).isNull();
        assertThat(s.get(Stage.TRUST).score()).isNull();
    }

    @Test
    void overall_ShouldWeightStages_OverThoseThatHaveAScore() {
        Map<Stage, StageScore> s = ScoreCalculator.stageScores(List.of(
                f(Stage.DISCOVER, CheckStatus.PASS),   // 100, weight 30
                f(Stage.TRUST, CheckStatus.FAIL),      // 0, weight 25
                f(Stage.BOOK, CheckStatus.PARTIAL)));  // 50, weight 25

        assertThat(ScoreCalculator.overall(s)).isEqualTo((int) Math.round((100 * 30 + 0 * 25 + 50 * 25) / 80.0));
    }

    @Test
    void overall_ShouldBeNull_WhenNothingIsKnown() {
        assertThat(ScoreCalculator.overall(ScoreCalculator.stageScores(List.of(f(Stage.BOOK, CheckStatus.UNKNOWN))))).isNull();
        assertThat(ScoreCalculator.overall(ScoreCalculator.stageScores(List.of()))).isNull();
    }
}
