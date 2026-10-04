package com.antenapro.hotelcheck.evidence;

import com.antenapro.hotelcheck.acquisition.AcquisitionResult;
import com.antenapro.hotelcheck.evaluation.EvaluationAcquisitionResult;

import java.util.Objects;

public final class EvidenceNormalizationService {

    public StructuredEvidence normalize(EvaluationAcquisitionResult result) {
        Objects.requireNonNull(result, "result must not be null");

        AcquisitionResult acquisition = result.acquisitionResult();
        EvidenceProvenance provenance = acquisition.isSuccess()
                ? EvidenceProvenance.DISCOVERED
                : EvidenceProvenance.DISCOVERED;

        return new StructuredEvidence(
                result.evaluation().evaluationId(),
                result.attempt().attemptId(),
                result.attempt().attemptNumber(),
                result,
                provenance,
                acquisition.outcome(),
                acquisition.requestedUrl(),
                acquisition.finalUrl(),
                acquisition.retrievalTimestamp(),
                acquisition.acquisitionMethod().name(),
                acquisition.statusCode(),
                acquisition.contentType(),
                acquisition.body(),
                acquisition.errorMessage()
        );
    }
}
