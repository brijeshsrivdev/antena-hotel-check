package com.antenapro.hotelcheck.acquisition;

import com.antenapro.hotelcheck.input.CanonicalEvaluationRequest;

public record AcquisitionRequest(
        String targetUrl,
        AcquisitionConfig config
) {
    public AcquisitionRequest {
        if (config == null) {
            config = AcquisitionConfig.defaults();
        }
    }

    public static AcquisitionRequest fromCanonicalRequest(CanonicalEvaluationRequest canonicalRequest) {
        return fromCanonicalRequest(canonicalRequest, AcquisitionConfig.defaults());
    }

    public static AcquisitionRequest fromCanonicalRequest(CanonicalEvaluationRequest canonicalRequest, AcquisitionConfig config) {
        if (canonicalRequest == null || canonicalRequest.evaluationTarget() == null) {
            throw new IllegalArgumentException("CanonicalEvaluationRequest and evaluationTarget must not be null");
        }
        String websiteUrl = canonicalRequest.evaluationTarget().websiteUrl();
        return new AcquisitionRequest(websiteUrl, config);
    }
}
