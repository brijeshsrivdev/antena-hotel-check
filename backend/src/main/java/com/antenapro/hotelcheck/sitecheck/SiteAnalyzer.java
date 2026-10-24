package com.antenapro.hotelcheck.sitecheck;

import com.antenapro.hotelcheck.sitecheck.check.SiteCheck;
import com.antenapro.hotelcheck.sitecheck.model.AnalysisResult;
import com.antenapro.hotelcheck.sitecheck.model.CheckStatus;
import com.antenapro.hotelcheck.sitecheck.model.Finding;
import com.antenapro.hotelcheck.sitecheck.model.SiteSnapshot;
import com.antenapro.hotelcheck.sitecheck.model.Stage;
import com.antenapro.hotelcheck.sitecheck.model.StageScore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** Runs the catalogue over a snapshot. A check that throws becomes an UNKNOWN finding, never a failed run. */
public class SiteAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(SiteAnalyzer.class);

    private final List<SiteCheck> checks;

    public SiteAnalyzer(List<SiteCheck> checks) {
        this.checks = List.copyOf(checks);
    }

    public AnalysisResult analyze(SiteSnapshot snapshot) {
        List<Finding> findings = new ArrayList<>();
        List<String> limitations = new ArrayList<>(snapshot.limitations());
        for (int i = 0; i < checks.size(); i++) {
            SiteCheck check = checks.get(i);
            try {
                findings.add(check.run(snapshot));
            } catch (RuntimeException e) {
                log.warn("Site check {} failed on {}: {}", check.id(), snapshot.host(), e.toString());
                findings.add(new Finding(check.id(), check.title(), check.stage(), CheckStatus.UNKNOWN,
                        "This check hit an error while reading the page.", "Run the check again.", "", false));
                limitations.add("\"" + check.title() + "\" could not be evaluated on this site.");
            }
        }
        List<Finding> sorted = new ArrayList<>(findings);
        sorted.sort(Comparator.comparingInt((Finding f) -> f.status().reportOrder())
                .thenComparingInt(f -> -f.stage().weight())
                .thenComparingInt(findings::indexOf));
        Map<Stage, StageScore> stages = ScoreCalculator.stageScores(findings);
        return new AnalysisResult(ScoreCalculator.overall(stages), stages, List.copyOf(sorted), snapshot.fetched(),
                List.copyOf(limitations), snapshot.home() == null ? null : snapshot.home().finalUrl());
    }

    /** Every check UNKNOWN: used when the run did not finish in time. */
    public AnalysisResult timedOut(SiteSnapshot partial, String limitation) {
        List<Finding> findings = checks.stream()
                .map(c -> new Finding(c.id(), c.title(), c.stage(), CheckStatus.UNKNOWN, "The run did not finish in time.", "Run the check again.", "", false))
                .toList();
        List<String> limitations = new ArrayList<>(partial == null ? List.of() : partial.limitations());
        limitations.add(limitation);
        return new AnalysisResult(null, ScoreCalculator.stageScores(findings), findings,
                partial == null ? List.of() : partial.fetched(), limitations, null);
    }
}
