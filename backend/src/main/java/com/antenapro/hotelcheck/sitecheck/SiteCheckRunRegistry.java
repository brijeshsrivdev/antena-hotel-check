package com.antenapro.hotelcheck.sitecheck;

import com.antenapro.hotelcheck.sitecheck.model.SiteCheckRun;

import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory runs (persistence is a later requirement). Runs are dropped after {@link #RETENTION}; the per-host
 * daily counter enforces "3 runs per host per day". There is no per-IP limit: the tool runs on one developer
 * machine (REQ-038).
 */
public class SiteCheckRunRegistry {

    static final Duration RETENTION = Duration.ofHours(2);
    private static final Duration HOST_WINDOW = Duration.ofDays(1);

    private final Clock clock;
    private final Map<UUID, SiteCheckRun> runs = new ConcurrentHashMap<>();
    private final Map<String, Deque<Instant>> runsPerHost = new ConcurrentHashMap<>();

    public SiteCheckRunRegistry(Clock clock) {
        this.clock = clock;
    }

    /** Counts a run against the host; false when the host already had {@code limit} runs in the last day. */
    public boolean tryReserve(String host, int limit) {
        Instant now = clock.instant();
        Deque<Instant> stamps = runsPerHost.computeIfAbsent(host, h -> new ArrayDeque<>());
        synchronized (stamps) {
            while (!stamps.isEmpty() && stamps.peekFirst().isBefore(now.minus(HOST_WINDOW))) {
                stamps.pollFirst();
            }
            if (stamps.size() >= limit) {
                return false;
            }
            stamps.addLast(now);
            return true;
        }
    }

    public SiteCheckRun create(URI requestedUrl) {
        evictExpired();
        SiteCheckRun run = new SiteCheckRun(UUID.randomUUID(), requestedUrl, clock.instant());
        runs.put(run.id(), run);
        return run;
    }

    public Optional<SiteCheckRun> find(UUID id) {
        return Optional.ofNullable(runs.get(id));
    }

    private void evictExpired() {
        Instant cutoff = clock.instant().minus(RETENTION);
        runs.values().removeIf(r -> r.startedAt().isBefore(cutoff));
    }
}
