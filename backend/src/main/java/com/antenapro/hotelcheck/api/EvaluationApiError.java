package com.antenapro.hotelcheck.api;

import com.antenapro.hotelcheck.input.InputValidationError;

import java.util.List;

public record EvaluationApiError(
        String code,
        String message,
        List<InputValidationError> errors
) {
    public EvaluationApiError {
        errors = errors == null ? List.of() : List.copyOf(errors);
    }
}
