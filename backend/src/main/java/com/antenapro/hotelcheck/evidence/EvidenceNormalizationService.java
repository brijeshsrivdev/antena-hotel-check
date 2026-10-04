package com.antenapro.hotelcheck.evidence;

import com.antenapro.hotelcheck.acquisition.AcquisitionResult;
import com.antenapro.hotelcheck.evaluation.EvaluationAcquisitionResult;

import java.util.Objects;

public final class EvidenceNormalizationService {

    public StructuredEvidence normalize(EvaluationAcquisitionResult result) {
        Objects.requireNonNull(result, "result must not be null");

        AcquisitionResult acquisition = result.acquisitionResult();

        return new StructuredEvidence(
                result.evaluation().evaluationId(),
                result.attempt().attemptId(),
                result.attempt().attemptNumber(),
                result,
                EvidenceProvenance.DISCOVERED,
                acquisition.outcome(),
                acquisition.requestedUrl(),
                acquisition.finalUrl(),
                acquisition.retrievalTimestamp(),
                acquisition.acquisitionMethod(),
                acquisition.statusCode(),
                acquisition.contentType(),
                acquisition.body(),
                acquisition.errorMessage()
        );
    }
}
