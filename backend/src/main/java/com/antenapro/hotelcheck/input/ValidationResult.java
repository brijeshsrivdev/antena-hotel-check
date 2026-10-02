package com.antenapro.hotelcheck.input;

import java.util.List;
import java.util.Objects;

public sealed interface ValidationResult permits ValidationResult.Accepted, ValidationResult.Rejected {

    record Accepted(CanonicalEvaluationRequest request) implements ValidationResult {
        public Accepted {
            Objects.requireNonNull(request, "request");
        }
    }

    record Rejected(List<InputValidationError> errors) implements ValidationResult {
        public Rejected {
            Objects.requireNonNull(errors, "errors");
            errors = List.copyOf(errors);
            if (errors.isEmpty()) {
                throw new IllegalArgumentException("At least one validation error is required");
            }
        }
    }
}
