package com.antenapro.hotelcheck.sitecheck;

import com.antenapro.hotelcheck.sitecheck.model.Finding;
import com.antenapro.hotelcheck.sitecheck.model.Stage;
import com.antenapro.hotelcheck.sitecheck.model.StageScore;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Stage score = mean of the stage's known checks (PASS 1, PARTIAL 0.5, FAIL 0; UNKNOWN excluded), as 0–100.
 * Overall = mean of the stage scores weighted by {@link Stage#weight()}, over the stages that have a score.
 * No decimals anywhere.
 */
public final class ScoreCalculator {

    private ScoreCalculator() {
    }

    public static Map<Stage, StageScore> stageScores(List<Finding> findings) {
        Map<Stage, StageScore> out = new EnumMap<>(Stage.class);
        for (Stage stage : Stage.values()) {
            int pass = 0;
            int partial = 0;
            int fail = 0;
            int unknown = 0;
            for (Finding f : findings) {
                if (f.stage() != stage) {
                    continue;
                }
                switch (f.status()) {
                    case PASS -> pass++;
                    case PARTIAL -> partial++;
                    case FAIL -> fail++;
                    case UNKNOWN -> unknown++;
                }
            }
            int known = pass + partial + fail;
            Integer score = known == 0 ? null : (int) Math.round((pass + 0.5 * partial) * 100.0 / known);
            out.put(stage, new StageScore(stage, score, pass, partial, fail, unknown));
        }
        return out;
    }

    public static Integer overall(Map<Stage, StageScore> stages) {
        double weighted = 0;
        int weights = 0;
        for (StageScore s : stages.values()) {
            if (s.score() == null) {
                continue;
            }
            weighted += s.score() * s.stage().weight();
            weights += s.stage().weight();
        }
        return weights == 0 ? null : (int) Math.round(weighted / weights);
    }
}
