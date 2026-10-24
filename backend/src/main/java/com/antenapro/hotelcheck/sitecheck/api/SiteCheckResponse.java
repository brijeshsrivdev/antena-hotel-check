package com.antenapro.hotelcheck.sitecheck.api;

import com.antenapro.hotelcheck.sitecheck.model.AnalysisResult;
import com.antenapro.hotelcheck.sitecheck.model.CheckStatus;
import com.antenapro.hotelcheck.sitecheck.model.FetchedPage;
import com.antenapro.hotelcheck.sitecheck.model.Finding;
import com.antenapro.hotelcheck.sitecheck.model.RunStatus;
import com.antenapro.hotelcheck.sitecheck.model.SiteCheckRun;
import com.antenapro.hotelcheck.sitecheck.model.Stage;
import com.antenapro.hotelcheck.sitecheck.model.StageScore;

import java.time.Instant;
import java.util.List;

/**
 * Body of {@code GET /api/site-checks/{runId}}. While RUNNING only the ids, status and timestamps
 * are set. The size stays far under the plan's 200 KB: evidence is capped per finding and no page is
 * included.
 */
public record SiteCheckResponse(String runId, RunStatus status, String requestedUrl, String finalUrl, Integer score,
                                List<StageDto> stages, List<FindingDto> findings, List<FetchedDto> fetched,
                                List<String> limitations, String failure, Instant startedAt, Instant finishedAt) {

    public record StageDto(Stage stage, String label, int weight, Integer score, int pass, int partial, int fail, int unknown) {
        static StageDto of(StageScore s) {
            return new StageDto(s.stage(), s.stage().label(), s.stage().weight(), s.score(), s.pass(), s.partial(), s.fail(), s.unknown());
        }
    }

    public record FindingDto(String check, String title, Stage stage, CheckStatus status, String evidence, String fix, String why,
                             boolean antenaHandles) {
        static FindingDto of(Finding f) {
            return new FindingDto(f.check(), f.title(), f.stage(), f.status(), f.evidence(), f.fix(), f.why(), f.antenaHandles());
        }
    }

    public record FetchedDto(String url, String finalUrl, String role, int status, long ms, long bytes, String error) {
        static FetchedDto of(FetchedPage p) {
            return new FetchedDto(p.requestedUrl(), p.finalUrl(), p.role().name().toLowerCase(), p.status(), p.ms(), p.bytes(), p.error());
        }
    }

    public static SiteCheckResponse of(SiteCheckRun run) {
        AnalysisResult r = run.result();
        if (r == null) {
            return new SiteCheckResponse(run.id().toString(), run.status(), run.requestedUrl().toString(), null, null,
                    List.of(), List.of(), List.of(), List.of(), run.failure(), run.startedAt(), run.finishedAt());
        }
        return new SiteCheckResponse(run.id().toString(), run.status(), run.requestedUrl().toString(), r.finalUrl(), r.score(),
                Stage.valuesInReportOrder().stream().map(r.stages()::get).map(StageDto::of).toList(),
                r.findings().stream().map(FindingDto::of).toList(),
                r.fetched().stream().map(FetchedDto::of).toList(),
                r.limitations(), run.failure(), run.startedAt(), run.finishedAt());
    }
}
