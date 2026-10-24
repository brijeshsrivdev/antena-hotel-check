package com.antenapro.hotelcheck.sitecheck.model;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

/**
 * One run in the in-memory registry. Transitions are RUNNING → DONE or RUNNING → FAILED, once; a late
 * completion after a timeout is ignored so the reported result never changes under the visitor.
 */
public final class SiteCheckRun {

    private final UUID id;
    private final URI requestedUrl;
    private final Instant startedAt;
    private RunStatus status = RunStatus.RUNNING;
    private AnalysisResult result;
    private String failure;
    private Instant finishedAt;

    public SiteCheckRun(UUID id, URI requestedUrl, Instant startedAt) {
        this.id = id;
        this.requestedUrl = requestedUrl;
        this.startedAt = startedAt;
    }

    public UUID id() {
        return id;
    }

    public URI requestedUrl() {
        return requestedUrl;
    }

    public Instant startedAt() {
        return startedAt;
    }

    public synchronized RunStatus status() {
        return status;
    }

    public synchronized AnalysisResult result() {
        return result;
    }

    public synchronized String failure() {
        return failure;
    }

    public synchronized Instant finishedAt() {
        return finishedAt;
    }

    /** @return false when the run had already finished (the new result is dropped). */
    public synchronized boolean complete(AnalysisResult newResult, Instant at) {
        if (status != RunStatus.RUNNING) {
            return false;
        }
        status = RunStatus.DONE;
        result = newResult;
        finishedAt = at;
        return true;
    }

    public synchronized boolean fail(String reason, Instant at) {
        if (status != RunStatus.RUNNING) {
            return false;
        }
        status = RunStatus.FAILED;
        failure = reason;
        finishedAt = at;
        return true;
    }
}
