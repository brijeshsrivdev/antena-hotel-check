package com.antenapro.hotelcheck.sitecheck;

import com.antenapro.hotelcheck.sitecheck.fetch.SiteSnapshotLoader;
import com.antenapro.hotelcheck.sitecheck.fetch.UrlPolicy;
import com.antenapro.hotelcheck.sitecheck.model.AnalysisResult;
import com.antenapro.hotelcheck.sitecheck.model.RunStatus;
import com.antenapro.hotelcheck.sitecheck.model.SiteCheckRun;
import com.antenapro.hotelcheck.sitecheck.model.SiteSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

/**
 * Starts a run (validate, count against the host, hand to the executor) and reads it back. Fetching stops at
 * about three quarters of the run timeout so the checks still have time; a run still RUNNING past the timeout
 * plus a grace period is reported DONE with every check UNKNOWN and a limitation line, as the plan requires.
 */
public class SiteCheckService {

    private static final Logger log = LoggerFactory.getLogger(SiteCheckService.class);

    private static final Duration GRACE = Duration.ofSeconds(10);

    private final SiteCheckProperties properties;
    private final UrlPolicy policy;
    private final SiteSnapshotLoader loader;
    private final SiteAnalyzer analyzer;
    private final SiteCheckRunRegistry registry;
    private final ExecutorService executor;
    private final Clock clock;

    public SiteCheckService(SiteCheckProperties properties, UrlPolicy policy, SiteSnapshotLoader loader, SiteAnalyzer analyzer,
                            SiteCheckRunRegistry registry, ExecutorService executor, Clock clock) {
        this.properties = properties;
        this.policy = policy;
        this.loader = loader;
        this.analyzer = analyzer;
        this.registry = registry;
        this.executor = executor;
        this.clock = clock;
    }

    /**
     * @throws com.antenapro.hotelcheck.sitecheck.fetch.InvalidUrlException when the URL may not be fetched
     * @throws HostLimitExceededException                          when the host hit its daily limit
     */
    public SiteCheckRun start(String rawUrl) {
        URI url = policy.normalize(rawUrl);
        String host = SiteSnapshot.hostOf(url.toString());
        if (!registry.tryReserve(host, properties.maxRunsPerHostPerDay())) {
            throw new HostLimitExceededException(host, properties.maxRunsPerHostPerDay());
        }
        SiteCheckRun run = registry.create(url);
        log.info("Site check started for {} (run {})", host, run.id());
        executor.submit(() -> execute(run));
        return run;
    }

    void execute(SiteCheckRun run) {
        Instant fetchDeadline = run.startedAt().plusMillis(properties.runTimeoutMs() * 3L / 4);
        SiteSnapshot snapshot = null;
        try {
            snapshot = loader.load(run.requestedUrl(), fetchDeadline);
            AnalysisResult result = analyzer.analyze(snapshot);
            if (!run.complete(result, clock.instant())) {
                log.info("Site check {} finished after it was reported as timed out", run.id());
            }
        } catch (RuntimeException e) {
            log.error("Site check {} failed", run.id(), e);
            run.fail("The analysis hit an internal error.", clock.instant());
        }
    }

    public Optional<SiteCheckRun> get(UUID id) {
        Optional<SiteCheckRun> found = registry.find(id);
        found.ifPresent(this::finishIfOverdue);
        return found;
    }

    private void finishIfOverdue(SiteCheckRun run) {
        if (run.status() != RunStatus.RUNNING) {
            return;
        }
        Instant limit = run.startedAt().plusMillis(properties.runTimeoutMs()).plus(GRACE);
        if (clock.instant().isAfter(limit)) {
            run.complete(analyzer.timedOut(null, "The site did not answer within " + properties.runTimeoutMs() / 1000
                    + " seconds, so no check could be completed."), clock.instant());
        }
    }
}
