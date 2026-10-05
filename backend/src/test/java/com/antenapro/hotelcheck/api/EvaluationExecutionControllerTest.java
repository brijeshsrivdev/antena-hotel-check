package com.antenapro.hotelcheck.api;

import com.antenapro.hotelcheck.evaluation.Evaluation;
import com.antenapro.hotelcheck.evaluation.EvaluationExecutionOrchestrator;
import com.antenapro.hotelcheck.evaluation.EvaluationExecutionResult;
import com.antenapro.hotelcheck.evaluation.EvaluationLifecycleState;
import com.antenapro.hotelcheck.evaluation.EvaluationAttempt;
import com.antenapro.hotelcheck.evaluation.OwnerFacingOutcome;
import com.antenapro.hotelcheck.input.CanonicalEvaluationRequest;
import com.antenapro.hotelcheck.input.HotelEvaluationInput;
import com.antenapro.hotelcheck.input.HotelEvaluationInputValidator;
import com.antenapro.hotelcheck.input.InputErrorCode;
import com.antenapro.hotelcheck.input.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EvaluationExecutionControllerTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-05T00:00:00Z"), ZoneOffset.UTC);
    private static final String WEBSITE = "https://hotel.example.com";

    private EvaluationExecutionOrchestrator orchestrator;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        orchestrator = mock(EvaluationExecutionOrchestrator.class);
        EvaluationExecutionController controller = new EvaluationExecutionController(
                new HotelEvaluationInputValidator(),
                orchestrator
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new EvaluationApiExceptionHandler())
                .build();
    }

    @Test
    void acceptsValidCanonicalInputAndDelegatesToOrchestrator() throws Exception {
        EvaluationExecutionResult result = executionResult();
        when(orchestrator.execute(any(CanonicalEvaluationRequest.class))).thenReturn(result);

        mockMvc.perform(post("/api/evaluations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"websiteUrl\":\"https://hotel.example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.evaluation.evaluationId").value(result.evaluation().evaluationId().toString()))
                .andExpect(jsonPath("$.attempt.state").value(EvaluationLifecycleState.INCOMPLETE.name()));

        var captor = org.mockito.ArgumentCaptor.forClass(CanonicalEvaluationRequest.class);
        verify(orchestrator).execute(captor.capture());
        assertEquals(WEBSITE, captor.getValue().evaluationTarget().websiteUrl());
    }

    @Test
    void rejectsMissingRequiredInputWithFourHundred() throws Exception {
        mockMvc.perform(post("/api/evaluations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_EVALUATION_REQUEST"))
                .andExpect(jsonPath("$.errors[0].code").exists());
    }

    @Test
    void rejectsBlankWebsiteUrlWithFourHundred() throws Exception {
        mockMvc.perform(post("/api/evaluations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"websiteUrl\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_EVALUATION_REQUEST"))
                .andExpect(jsonPath("$.errors[0].code").value(InputErrorCode.NO_USABLE_INPUT.name()));
    }

    @Test
    void rejectsStructurallyInvalidWebsiteUrlWithFourHundred() throws Exception {
        mockMvc.perform(post("/api/evaluations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"websiteUrl\":\"not-a-url\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_EVALUATION_REQUEST"))
                .andExpect(jsonPath("$.errors[0].code").value(InputErrorCode.UNSUPPORTED_WEBSITE_URL_FORM.name()));
    }

    @Test
    void returnsBadRequestForMalformedJson() throws Exception {
        mockMvc.perform(post("/api/evaluations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"websiteUrl\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST_BODY"));
    }

    @Test
    void doesNotReturnFakeSuccessWhenOrchestratorFails() throws Exception {
        when(orchestrator.execute(any(CanonicalEvaluationRequest.class)))
                .thenThrow(new IllegalStateException("internal execution failure"));

        mockMvc.perform(post("/api/evaluations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"websiteUrl\":\"https://hotel.example.com\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("EVALUATION_EXECUTION_FAILED"))
                .andExpect(jsonPath("$.evaluation").doesNotExist());
    }

    private EvaluationExecutionResult executionResult() {
        CanonicalEvaluationRequest request = acceptedRequest();
        Evaluation evaluation = Evaluation.create(request, CLOCK);
        evaluation.start();
        EvaluationAttempt attempt = evaluation.currentAttempt();
        attempt.incomplete("analysis complete", new OwnerFacingOutcome(true, false));

        EvaluationExecutionResult result = mock(EvaluationExecutionResult.class);
        when(result.evaluation()).thenReturn(evaluation);
        when(result.attempt()).thenReturn(attempt);
        return result;
    }

    private CanonicalEvaluationRequest acceptedRequest() {
        ValidationResult result = new HotelEvaluationInputValidator().validate(
                new HotelEvaluationInput(null, null, WEBSITE));
        return ((ValidationResult.Accepted) result).request();
    }
}
