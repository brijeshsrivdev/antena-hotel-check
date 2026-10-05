package com.antenapro.hotelcheck.api;

import com.antenapro.hotelcheck.input.InputValidationError;

import java.util.List;
import java.util.Objects;

final class InvalidEvaluationRequestException extends RuntimeException {
    private final List<InputValidationError> errors;

    InvalidEvaluationRequestException(List<InputValidationError> errors) {
        super("Evaluation request is invalid");
        this.errors = List.copyOf(Objects.requireNonNull(errors, "errors"));
    }

    List<InputValidationError> errors() {
        return errors;
    }
}
