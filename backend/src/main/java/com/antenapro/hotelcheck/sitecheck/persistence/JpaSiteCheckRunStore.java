package com.antenapro.hotelcheck.sitecheck.persistence;

import com.antenapro.hotelcheck.sitecheck.SiteCheckRunStore;
import com.antenapro.hotelcheck.sitecheck.api.SiteCheckResponse;
import com.antenapro.hotelcheck.sitecheck.model.AnalysisResult;
import com.antenapro.hotelcheck.sitecheck.model.SiteCheckRun;
import com.antenapro.hotelcheck.sitecheck.model.SiteSnapshot;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** JPA-backed store: the stage and finding DTOs of the API response are what gets serialised, so the two never drift. */
@Transactional
public class JpaSiteCheckRunStore implements SiteCheckRunStore {

    private final SpringDataSiteCheckRunRepository repository;
    private final ObjectMapper objectMapper;

    public JpaSiteCheckRunStore(SpringDataSiteCheckRunRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void save(SiteCheckRun run, String ipHash) {
        AnalysisResult result = run.result();
        SiteCheckResponse view = SiteCheckResponse.of(run);
        try {
            repository.save(new SiteCheckRunJpaEntity(
                    run.id(),
                    run.requestedUrl().toString(),
                    result == null ? null : result.finalUrl(),
                    SiteSnapshot.hostOf(run.requestedUrl().toString()),
                    run.status().name(),
                    result == null ? null : result.score(),
                    objectMapper.writeValueAsString(view.stages()),
                    objectMapper.writeValueAsString(view.findings()),
                    objectMapper.writeValueAsString(result == null ? List.of(run.failure() == null ? "" : run.failure()) : result.limitations()),
                    ipHash,
                    run.startedAt()));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialise the site check result", e);
        }
    }

    @Override
    public boolean recordConsent(UUID runId, String email, Instant consentAt) {
        return repository.findById(runId).map(entity -> {
            entity.recordConsent(email, consentAt);
            repository.save(entity);
            return true;
        }).orElse(false);
    }
}
