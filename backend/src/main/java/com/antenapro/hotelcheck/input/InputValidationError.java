package com.antenapro.hotelcheck.input;

public record InputValidationError(
        InputErrorCode code,
        String field,
        String message
) {
}
