package com.antenapro.hotelcheck.evaluation;

import java.util.Optional;
import java.util.UUID;

public interface EvaluationRepository {
    Evaluation save(Evaluation evaluation);

    Optional<Evaluation> findById(UUID evaluationId);
}
