package com.antenapro.hotelcheck.acquisition;

import com.antenapro.hotelcheck.input.CanonicalEvaluationRequest;

public interface PublicWebAcquisitionService {
    AcquisitionResult acquire(AcquisitionRequest request);

    default AcquisitionResult acquire(CanonicalEvaluationRequest canonicalRequest) {
        return acquire(AcquisitionRequest.fromCanonicalRequest(canonicalRequest));
    }
}
