package com.antenapro.hotelcheck.input;

/**
 * Raw user-supplied values at the evaluation input boundary.
 */
public record HotelEvaluationInput(
        String hotelName,
        String city,
        String websiteUrl
) {
}
