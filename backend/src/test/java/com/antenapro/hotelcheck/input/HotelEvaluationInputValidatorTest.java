package com.antenapro.hotelcheck.input;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HotelEvaluationInputValidatorTest {

    private final HotelEvaluationInputValidator validator = new HotelEvaluationInputValidator();

    @Test
    void acceptsHotelNameAndCity() {
        ValidationResult.Accepted result = accepted(new HotelEvaluationInput("The Grand Hotel", "Mumbai", null));

        assertEquals(EvaluationTargetType.HOTEL_IDENTITY, result.request().evaluationTarget().targetType());
        assertEquals("The Grand Hotel", result.request().normalizedInput().hotelName());
        assertEquals("Mumbai", result.request().normalizedInput().city());
        assertNull(result.request().normalizedInput().websiteUrl());
    }

    @Test
    void acceptsWebsiteOnly() {
        ValidationResult.Accepted result = accepted(new HotelEvaluationInput(null, null, "https://example.com/hotel"));

        assertEquals(EvaluationTargetType.WEBSITE, result.request().evaluationTarget().targetType());
        assertEquals("https://example.com/hotel", result.request().normalizedInput().websiteUrl());
        assertNull(result.request().evaluationTarget().identityContext());
    }

    @Test
    void acceptsCompleteCombinedInputAndRetainsIdentityContext() {
        ValidationResult.Accepted result = accepted(new HotelEvaluationInput(
                "The Grand Hotel", "Mumbai", "https://example.com/hotel#rooms"));

        CanonicalEvaluationRequest request = result.request();
        assertEquals(EvaluationTargetType.WEBSITE, request.evaluationTarget().targetType());
        assertEquals("https://example.com/hotel", request.normalizedInput().websiteUrl());
        assertEquals(new CanonicalEvaluationRequest.IdentityContext("The Grand Hotel", "Mumbai"),
                request.evaluationTarget().identityContext());
        assertEquals("The Grand Hotel", request.requestInput().hotelNameOriginal());
        assertEquals("https://example.com/hotel#rooms", request.requestInput().websiteUrlOriginal());
    }

    @Test
    void trimsAndCollapsesTextWhitespaceWithoutLosingOriginalValues() {
        ValidationResult.Accepted result = accepted(new HotelEvaluationInput(
                "  The   Grand  Hotel  ", "  New   Delhi  ", null));

        assertEquals("The Grand Hotel", result.request().normalizedInput().hotelName());
        assertEquals("New Delhi", result.request().normalizedInput().city());
        assertEquals("  The   Grand  Hotel  ", result.request().requestInput().hotelNameOriginal());
    }

    @Test
    void acceptsUnicodeNormalizedText() {
        ValidationResult.Accepted result = accepted(new HotelEvaluationInput("Cafe\u0301 Hotel", "Pune", null));

        assertEquals("Café Hotel", result.request().normalizedInput().hotelName());
    }

    @Test
    void casingDifferencesDoNotRejectOtherwiseValidIdentityInput() {
        ValidationResult.Accepted result = accepted(new HotelEvaluationInput("THE GRAND HOTEL", "MUMBAI", null));

        assertEquals("THE GRAND HOTEL", result.request().normalizedInput().hotelName());
        assertEquals("MUMBAI", result.request().normalizedInput().city());
    }

    @Test
    void rejectsHotelNameWithoutCity() {
        ValidationResult.Rejected result = rejected(new HotelEvaluationInput("The Grand Hotel", null, null));

        assertCodes(result, InputErrorCode.MISSING_CITY, InputErrorCode.INCOMPLETE_HOTEL_IDENTITY);
    }

    @Test
    void rejectsCityWithoutHotelName() {
        ValidationResult.Rejected result = rejected(new HotelEvaluationInput(null, "Mumbai", null));

        assertCodes(result, InputErrorCode.MISSING_HOTEL_NAME, InputErrorCode.INCOMPLETE_HOTEL_IDENTITY);
    }

    @Test
    void rejectsWhitespaceOnlyInput() {
        ValidationResult.Rejected result = rejected(new HotelEvaluationInput("  ", "\t", "  "));

        assertCodes(result, InputErrorCode.NO_USABLE_INPUT);
    }

    @Test
    void rejectsPartialIdentityCombinedWithWebsite() {
        ValidationResult.Rejected result = rejected(new HotelEvaluationInput("The Grand Hotel", null, "https://example.com"));

        assertCodes(result,
                InputErrorCode.MISSING_CITY,
                InputErrorCode.INCOMPLETE_HOTEL_IDENTITY,
                InputErrorCode.INVALID_INPUT_COMBINATION);
    }

    @Test
    void rejectsRelativeWebsiteUrl() {
        ValidationResult.Rejected result = rejected(new HotelEvaluationInput(null, null, "/hotel"));

        assertCodes(result, InputErrorCode.UNSUPPORTED_WEBSITE_URL_FORM);
    }

    @Test
    void rejectsProtocolRelativeWebsiteUrl() {
        ValidationResult.Rejected result = rejected(new HotelEvaluationInput(null, null, "//example.com/hotel"));

        assertCodes(result, InputErrorCode.UNSUPPORTED_WEBSITE_URL_FORM);
    }

    @Test
    void rejectsNonHttpWebsiteUrl() {
        ValidationResult.Rejected result = rejected(new HotelEvaluationInput(null, null, "javascript:alert(1)"));

        assertCodes(result, InputErrorCode.UNSUPPORTED_WEBSITE_URL_FORM);
    }

    @Test
    void rejectsCredentialBearingWebsiteUrl() {
        ValidationResult.Rejected result = rejected(new HotelEvaluationInput(null, null, "https://user:password@example.com/hotel"));

        assertCodes(result, InputErrorCode.UNSUPPORTED_WEBSITE_URL_FORM);
    }

    @Test
    void rejectsMalformedWebsiteUrl() {
        ValidationResult.Rejected result = rejected(new HotelEvaluationInput(null, null, "https://example.com:bad"));

        assertCodes(result, InputErrorCode.MALFORMED_WEBSITE_URL);
    }

    @Test
    void rejectsWebsiteUrlWithInvalidHost() {
        ValidationResult.Rejected result = rejected(new HotelEvaluationInput(null, null, "https:///hotel"));

        assertCodes(result, InputErrorCode.MALFORMED_WEBSITE_URL);
    }

    @Test
    void normalizesHttpSchemeAndHostCasingAndRemovesFragment() {
        ValidationResult.Accepted result = accepted(new HotelEvaluationInput(
                null, null, "  HTTPS://EXAMPLE.COM/hotel?lang=en#rooms  "));

        assertEquals("https://example.com/hotel?lang=en", result.request().normalizedInput().websiteUrl());
    }

    @Test
    void preservesWebsitePathAndQuery() {
        ValidationResult.Accepted result = accepted(new HotelEvaluationInput(
                null, null, "https://example.com/hotel/rooms?foo=bar%20baz"));

        assertEquals("https://example.com/hotel/rooms?foo=bar%20baz",
                result.request().normalizedInput().websiteUrl());
    }

    @Test
    void websiteUrlIsOptionalForIdentityInput() {
        ValidationResult.Accepted result = accepted(new HotelEvaluationInput("The Grand Hotel", "Mumbai", null));

        assertEquals(EvaluationTargetType.HOTEL_IDENTITY, result.request().evaluationTarget().targetType());
    }

    @Test
    void hotelIdentityIsOptionalForWebsiteInput() {
        ValidationResult.Accepted result = accepted(new HotelEvaluationInput(null, null, "https://example.com"));

        assertEquals(EvaluationTargetType.WEBSITE, result.request().evaluationTarget().targetType());
    }

    @Test
    void validatorDoesNotNeedNetworkAccessToAcceptUrl() {
        ValidationResult.Accepted result = accepted(new HotelEvaluationInput(null, null, "https://nonexistent.example"));

        assertTrue(result.request().normalizedInput().websiteUrl().startsWith("https://"));
    }

    private ValidationResult.Accepted accepted(HotelEvaluationInput input) {
        return assertInstanceOf(ValidationResult.Accepted.class, validator.validate(input));
    }

    private ValidationResult.Rejected rejected(HotelEvaluationInput input) {
        return assertInstanceOf(ValidationResult.Rejected.class, validator.validate(input));
    }

    private void assertCodes(ValidationResult.Rejected result, InputErrorCode... expected) {
        List<InputErrorCode> actual = result.errors().stream().map(InputValidationError::code).toList();
        assertEquals(List.of(expected), actual);
        assertFalse(actual.isEmpty());
    }
}
