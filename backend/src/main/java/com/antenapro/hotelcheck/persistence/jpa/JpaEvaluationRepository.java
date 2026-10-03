package com.antenapro.hotelcheck.persistence.jpa;

import com.antenapro.hotelcheck.evaluation.Evaluation;
import com.antenapro.hotelcheck.evaluation.EvaluationRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
public class JpaEvaluationRepository implements EvaluationRepository {
    private final SpringDataEvaluationRepository repository;

    public JpaEvaluationRepository(SpringDataEvaluationRepository repository) {
        this.repository = repository;
    }

    @Override
    public Evaluation save(Evaluation evaluation) {
        EvaluationJpaEntity entity = EvaluationJpaEntity.fromDomain(evaluation);
        return repository.saveAndFlush(entity).toDomain();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Evaluation> findById(UUID evaluationId) {
        return repository.findById(evaluationId).map(EvaluationJpaEntity::toDomain);
    }
}
