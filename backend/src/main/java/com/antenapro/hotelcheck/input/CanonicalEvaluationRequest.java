package com.antenapro.hotelcheck.input;

public record CanonicalEvaluationRequest(
        RequestInput requestInput,
        NormalizedInput normalizedInput,
        EvaluationTarget evaluationTarget
) {
    public record RequestInput(
            String hotelNameOriginal,
            String cityOriginal,
            String websiteUrlOriginal
    ) {
    }

    public record NormalizedInput(
            String hotelName,
            String city,
            String websiteUrl
    ) {
    }

    public record EvaluationTarget(
            EvaluationTargetType targetType,
            String hotelName,
            String city,
            String websiteUrl,
            IdentityContext identityContext
    ) {
    }

    public record IdentityContext(
            String hotelName,
            String city
    ) {
    }
}
