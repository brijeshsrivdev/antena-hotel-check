package com.antenapro.hotelcheck.sitecheck;

import com.antenapro.hotelcheck.sitecheck.model.SiteCheckRun;

import java.time.Instant;
import java.util.UUID;

/** Durable record of finished runs and of the consent a visitor gave for the PDF. Never stores page HTML or an IP. */
public interface SiteCheckRunStore {

    /** Persists a DONE or FAILED run; a store failure is logged by the caller and never fails the run. */
    void save(SiteCheckRun run, String ipHash);

    /** Records the visitor's email and consent time (source {@code hotel_check_form}); false when the run is not stored. */
    boolean recordConsent(UUID runId, String email, Instant consentAt);

    /** A store that keeps nothing, for unit tests and for running without a database. */
    SiteCheckRunStore NONE = new SiteCheckRunStore() {
        @Override
        public void save(SiteCheckRun run, String ipHash) {
        }

        @Override
        public boolean recordConsent(UUID runId, String email, Instant consentAt) {
            return false;
        }
    };
}
