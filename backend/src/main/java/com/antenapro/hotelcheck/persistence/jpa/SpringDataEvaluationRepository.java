package com.antenapro.hotelcheck.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface SpringDataEvaluationRepository extends JpaRepository<EvaluationJpaEntity, UUID> {
}
