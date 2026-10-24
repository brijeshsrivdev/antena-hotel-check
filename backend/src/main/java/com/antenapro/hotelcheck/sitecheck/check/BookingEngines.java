package com.antenapro.hotelcheck.sitecheck.check;

import com.antenapro.hotelcheck.sitecheck.html.HtmlSupport;

import java.util.List;

/** Host fragments that identify a direct booking engine versus an online travel agency (OTA). */
final class BookingEngines {

    static final List<String> ENGINES = List.of(
            "staah", "ezee", "siteminder", "thebookingbutton", "littlehotelier", "djubo", "resavenue", "axisrooms", "aiosell",
            "maximojo", "simplotel", "eglobe", "hotelogix", "hotelrunner", "cloudbeds", "mews", "roomraccoon", "bookvisit",
            "profitroom", "sabeeapp", "bookingwhizz", "hoteliga", "bookone", "wincloud", "reservationdesk", "bookingengine", "antenapro.com");

    static final List<String> OTAS = List.of(
            "booking.com", "agoda", "makemytrip", "goibibo", "expedia", "hotels.com", "airbnb", "oyorooms", "tripadvisor",
            "trivago", "cleartrip", "yatra", "easemytrip", "ixigo", "hostelworld", "trip.com", "travelguru", "stayzilla");

    private BookingEngines() {
    }

    static boolean isEngine(String url) {
        String h = HtmlSupport.host(url);
        return ENGINES.stream().anyMatch(h::contains);
    }

    static boolean isOta(String url) {
        String h = HtmlSupport.host(url);
        return OTAS.stream().anyMatch(h::contains);
    }
}
