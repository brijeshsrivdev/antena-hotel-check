package com.antenapro.hotelcheck.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Minimal API error boundary for request and execution failures. */
@RestControllerAdvice
public final class EvaluationApiExceptionHandler {

    @ExceptionHandler(InvalidEvaluationRequestException.class)
    ResponseEntity<EvaluationApiError> handleInvalidRequest(InvalidEvaluationRequestException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new EvaluationApiError(
                        "INVALID_EVALUATION_REQUEST",
                        "The evaluation request is invalid",
                        exception.errors()
                )
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<EvaluationApiError> handleUnreadableRequest(HttpMessageNotReadableException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new EvaluationApiError(
                        "INVALID_REQUEST_BODY",
                        "The request body must be valid JSON matching the evaluation input contract",
                        null
                )
        );
    }

    @ExceptionHandler(RuntimeException.class)
    ResponseEntity<EvaluationApiError> handleExecutionFailure(RuntimeException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new EvaluationApiError(
                        "EVALUATION_EXECUTION_FAILED",
                        "Evaluation execution failed",
                        null
                )
        );
    }
}
