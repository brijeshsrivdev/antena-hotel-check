package com.antenapro.hotelcheck.input;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Validates and canonicalizes the two supported evaluation input modes.
 * This class deliberately performs no network access or hotel resolution.
 */
public final class HotelEvaluationInputValidator {

    public ValidationResult validate(HotelEvaluationInput input) {
        if (input == null) {
            return rejected(new InputValidationError(
                    InputErrorCode.NO_USABLE_INPUT,
                    null,
                    "At least a hotel name and city or a website URL is required"));
        }

        String hotelName = normalizeText(input.hotelName());
        String city = normalizeText(input.city());
        String websiteUrl = normalizeWebsiteInput(input.websiteUrl());

        boolean hasHotelName = hasValue(hotelName);
        boolean hasCity = hasValue(city);
        boolean hasWebsiteUrl = hasValue(websiteUrl);

        if (!hasHotelName && !hasCity && !hasWebsiteUrl) {
            return rejected(new InputValidationError(
                    InputErrorCode.NO_USABLE_INPUT,
                    null,
                    "At least a hotel name and city or a website URL is required"));
        }

        if (hasWebsiteUrl && (hasHotelName ^ hasCity)) {
            List<InputValidationError> errors = new ArrayList<>();
            if (!hasHotelName) {
                errors.add(new InputValidationError(
                        InputErrorCode.MISSING_HOTEL_NAME,
                        "hotelName",
                        "Hotel name is required when identity context is supplied with a website URL"));
            }
            if (!hasCity) {
                errors.add(new InputValidationError(
                        InputErrorCode.MISSING_CITY,
                        "city",
                        "City is required when identity context is supplied with a website URL"));
            }
            errors.add(new InputValidationError(
                    InputErrorCode.INCOMPLETE_HOTEL_IDENTITY,
                    "hotelName/city",
                    "Hotel name and city must be supplied together"));
            errors.add(new InputValidationError(
                    InputErrorCode.INVALID_INPUT_COMBINATION,
                    "hotelName/city/websiteUrl",
                    "A website URL may be combined only with a complete hotel name and city"));
            return new ValidationResult.Rejected(errors);
        }

        if (hasWebsiteUrl) {
            UrlValidationResult urlResult = validateWebsiteUrl(websiteUrl);
            if (!urlResult.valid()) {
                return rejected(new InputValidationError(
                        urlResult.errorCode(),
                        "websiteUrl",
                        urlResult.message()));
            }

            CanonicalEvaluationRequest.IdentityContext identityContext = hasHotelName
                    ? new CanonicalEvaluationRequest.IdentityContext(hotelName, city)
                    : null;
            CanonicalEvaluationRequest.EvaluationTarget target = new CanonicalEvaluationRequest.EvaluationTarget(
                    EvaluationTargetType.WEBSITE,
                    null,
                    null,
                    urlResult.normalizedUrl(),
                    identityContext);

            return new ValidationResult.Accepted(canonicalRequest(
                    input,
                    hotelName,
                    city,
                    urlResult.normalizedUrl(),
                    target));
        }

        if (hasHotelName && hasCity) {
            CanonicalEvaluationRequest.EvaluationTarget target = new CanonicalEvaluationRequest.EvaluationTarget(
                    EvaluationTargetType.HOTEL_IDENTITY,
                    hotelName,
                    city,
                    null,
                    null);

            return new ValidationResult.Accepted(canonicalRequest(
                    input,
                    hotelName,
                    city,
                    null,
                    target));
        }

        if (!hasHotelName) {
            return new ValidationResult.Rejected(List.of(
                    new InputValidationError(
                            InputErrorCode.MISSING_HOTEL_NAME,
                            "hotelName",
                            "Hotel name is required for hotel-identity input"),
                    new InputValidationError(
                            InputErrorCode.INCOMPLETE_HOTEL_IDENTITY,
                            "hotelName/city",
                            "Hotel name and city must be supplied together")));
        }

        return new ValidationResult.Rejected(List.of(
                new InputValidationError(
                        InputErrorCode.MISSING_CITY,
                        "city",
                        "City is required for hotel-identity input"),
                new InputValidationError(
                        InputErrorCode.INCOMPLETE_HOTEL_IDENTITY,
                        "hotelName/city",
                        "Hotel name and city must be supplied together")));
    }

    private CanonicalEvaluationRequest canonicalRequest(
            HotelEvaluationInput original,
            String hotelName,
            String city,
            String websiteUrl,
            CanonicalEvaluationRequest.EvaluationTarget target) {
        return new CanonicalEvaluationRequest(
                new CanonicalEvaluationRequest.RequestInput(
                        original.hotelName(),
                        original.city(),
                        original.websiteUrl()),
                new CanonicalEvaluationRequest.NormalizedInput(hotelName, city, websiteUrl),
                target);
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        return Normalizer.normalize(value.trim().replaceAll("\\s+", " "), Normalizer.Form.NFC);
    }

    private String normalizeWebsiteInput(String value) {
        return value == null ? null : value.trim();
    }

    private boolean hasValue(String value) {
        return value != null && !value.isBlank();
    }

    private ValidationResult.Rejected rejected(InputValidationError error) {
        return new ValidationResult.Rejected(List.of(error));
    }

    private UrlValidationResult validateWebsiteUrl(String value) {
        URI uri;
        try {
            uri = new URI(value);
        } catch (URISyntaxException ex) {
            return UrlValidationResult.invalid(
                    InputErrorCode.MALFORMED_WEBSITE_URL,
                    "Website URL is not syntactically valid");
        }

        String scheme = uri.getScheme();
        if (!uri.isAbsolute() || scheme == null
                || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            return UrlValidationResult.invalid(
                    InputErrorCode.UNSUPPORTED_WEBSITE_URL_FORM,
                    "Website URL must be an absolute HTTP or HTTPS URL");
        }

        if (uri.getUserInfo() != null) {
            return UrlValidationResult.invalid(
                    InputErrorCode.UNSUPPORTED_WEBSITE_URL_FORM,
                    "Website URL must not contain username or password credentials");
        }

        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            return UrlValidationResult.invalid(
                    InputErrorCode.MALFORMED_WEBSITE_URL,
                    "Website URL must contain a syntactically valid host");
        }

        String normalizedHost = host.toLowerCase(Locale.ROOT);
        String authority = formatAuthority(normalizedHost, uri.getPort());
        String normalizedUrl = scheme.toLowerCase(Locale.ROOT)
                + "://"
                + authority
                + nullToEmpty(uri.getRawPath())
                + (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery());

        return UrlValidationResult.valid(normalizedUrl);
    }

    private String formatAuthority(String host, int port) {
        String formattedHost = host;
        if (host.contains(":") && !(host.startsWith("[") && host.endsWith("]"))) {
            formattedHost = "[" + host + "]";
        }
        return formattedHost + (port >= 0 ? ":" + port : "");
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private record UrlValidationResult(boolean valid, String normalizedUrl,
                                       InputErrorCode errorCode, String message) {
        private static UrlValidationResult valid(String normalizedUrl) {
            return new UrlValidationResult(true, normalizedUrl, null, null);
        }

        private static UrlValidationResult invalid(InputErrorCode errorCode, String message) {
            return new UrlValidationResult(false, null, errorCode, message);
        }
    }
}
