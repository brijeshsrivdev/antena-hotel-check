package com.antenapro.hotelcheck.persistence.jpa;

import com.antenapro.hotelcheck.evaluation.Evaluation;
import com.antenapro.hotelcheck.evaluation.EvaluationAttempt;
import com.antenapro.hotelcheck.input.CanonicalEvaluationRequest;
import com.antenapro.hotelcheck.input.EvaluationTargetType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "evaluations")
public class EvaluationJpaEntity {
    @Id
    private UUID id;

    private String hotelNameOriginal;
    private String cityOriginal;
    private String websiteUrlOriginal;
    private String hotelName;
    private String city;
    private String websiteUrl;
    private EvaluationTargetType targetType;
    private String targetHotelName;
    private String targetCity;
    private String targetWebsiteUrl;
    private String identityHotelName;
    private String identityCity;

    @OneToMany(mappedBy = "evaluation", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("attemptNumber ASC")
    private List<EvaluationAttemptJpaEntity> attempts = new ArrayList<>();

    protected EvaluationJpaEntity() {
    }

    public static EvaluationJpaEntity fromDomain(Evaluation evaluation) {
        CanonicalEvaluationRequest request = evaluation.request();
        EvaluationJpaEntity entity = new EvaluationJpaEntity();
        entity.id = evaluation.evaluationId();
        entity.hotelNameOriginal = request.requestInput().hotelNameOriginal();
        entity.cityOriginal = request.requestInput().cityOriginal();
        entity.websiteUrlOriginal = request.requestInput().websiteUrlOriginal();
        entity.hotelName = request.normalizedInput().hotelName();
        entity.city = request.normalizedInput().city();
        entity.websiteUrl = request.normalizedInput().websiteUrl();
        entity.targetType = request.evaluationTarget().targetType();
        entity.targetHotelName = request.evaluationTarget().hotelName();
        entity.targetCity = request.evaluationTarget().city();
        entity.targetWebsiteUrl = request.evaluationTarget().websiteUrl();
        entity.identityHotelName = request.evaluationTarget().identityContext() == null
                ? null : request.evaluationTarget().identityContext().hotelName();
        entity.identityCity = request.evaluationTarget().identityContext() == null
                ? null : request.evaluationTarget().identityContext().city();
        for (EvaluationAttempt attempt : evaluation.attempts()) {
            entity.attempts.add(EvaluationAttemptJpaEntity.fromDomain(entity, attempt));
        }
        return entity;
    }

    public Evaluation toDomain() {
        CanonicalEvaluationRequest request = new CanonicalEvaluationRequest(
                new CanonicalEvaluationRequest.RequestInput(hotelNameOriginal, cityOriginal, websiteUrlOriginal),
                new CanonicalEvaluationRequest.NormalizedInput(hotelName, city, websiteUrl),
                new CanonicalEvaluationRequest.EvaluationTarget(
                        targetType, targetHotelName, targetCity, targetWebsiteUrl,
                        new CanonicalEvaluationRequest.IdentityContext(identityHotelName, identityCity)
                )
        );
        List<EvaluationAttempt> domainAttempts = attempts.stream()
                .map(attempt -> attempt.toDomain(request))
                .toList();
        return Evaluation.rehydrate(id, request, domainAttempts, Clock.systemUTC());
    }

    public UUID getId() { return id; }
}
