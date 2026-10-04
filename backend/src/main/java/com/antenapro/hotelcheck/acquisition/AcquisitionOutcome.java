package com.antenapro.hotelcheck.acquisition;

public enum AcquisitionOutcome {
    SUCCESS,
    HTTP_ERROR,
    TIMEOUT,
    REDIRECT_LIMIT_EXCEEDED,
    RESPONSE_TOO_LARGE,
    UNSUPPORTED_SCHEME,
    NETWORK_ERROR,
    INVALID_TARGET
}
