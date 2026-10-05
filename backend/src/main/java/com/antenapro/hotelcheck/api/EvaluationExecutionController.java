package com.antenapro.hotelcheck.api;

import com.antenapro.hotelcheck.evaluation.EvaluationExecutionOrchestrator;
import com.antenapro.hotelcheck.evaluation.EvaluationExecutionResult;
import com.antenapro.hotelcheck.input.HotelEvaluationInput;
import com.antenapro.hotelcheck.input.HotelEvaluationInputValidator;
import com.antenapro.hotelcheck.input.ValidationResult;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Thin HTTP boundary for synchronous evaluation execution.
 *
 * <p>Input canonicalization remains owned by the existing validator and all
 * evaluation semantics remain owned by the existing orchestration pipeline.</p>
 */
@RestController
@RequestMapping("/api/evaluations")
public final class EvaluationExecutionController {

    private final HotelEvaluationInputValidator inputValidator;
    private final EvaluationExecutionOrchestrator orchestrator;

    public EvaluationExecutionController(
            HotelEvaluationInputValidator inputValidator,
            EvaluationExecutionOrchestrator orchestrator
    ) {
        this.inputValidator = inputValidator;
        this.orchestrator = orchestrator;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EvaluationExecutionResponse> execute(
            @RequestBody(required = false) HotelEvaluationInput input
    ) {
        ValidationResult validation = inputValidator.validate(input);
        if (validation instanceof ValidationResult.Rejected rejected) {
            throw new InvalidEvaluationRequestException(rejected.errors());
        }

        CanonicalRequestHolder accepted = accepted(validation);
        EvaluationExecutionResult result = orchestrator.execute(accepted.request());
        return ResponseEntity.ok(EvaluationExecutionResponse.from(result));
    }

    private CanonicalRequestHolder accepted(ValidationResult validation) {
        if (validation instanceof ValidationResult.Accepted accepted) {
            return new CanonicalRequestHolder(accepted.request());
        }
        throw new IllegalStateException("Validation result did not contain an accepted request");
    }

    private record CanonicalRequestHolder(com.antenapro.hotelcheck.input.CanonicalEvaluationRequest request) {
    }
}
