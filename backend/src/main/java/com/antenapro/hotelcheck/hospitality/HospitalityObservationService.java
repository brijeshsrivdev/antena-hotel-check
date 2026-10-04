package com.antenapro.hotelcheck.hospitality;

import com.antenapro.hotelcheck.evidence.EvidenceProvenance;
import com.antenapro.hotelcheck.evidence.StructuredEvidence;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HospitalityObservationService {

    private static final Pattern SEMANTIC_ELEMENT = Pattern.compile(
            "(?is)<(title|h1|h2|h3|a|address)\\b([^>]*)>(.*?)</\\1\\s*>"
    );
    private static final Pattern HREF = Pattern.compile("(?is)\\bhref\\s*=\\s*([\\\"'])(.*?)\\1");
    private static final Pattern HOTEL_IDENTITY = Pattern.compile(
            "(?i)\\b(?:hotel|resort|inn|lodge|guesthouse|guest\\s+house|boutique\\s+hotel|homestay)\\b"
    );
    private static final Pattern ROOMS = Pattern.compile(
            "(?i)\\b(?:rooms?|suites?|accommodations?|room\\s+types?)\\b"
    );
    private static final Pattern ROOM_NEGATIVE = Pattern.compile(
            "(?i)\\b(?:conference|meeting|training|server|locker|mail)\\s+rooms?\\b"
    );
    private static final Pattern AMENITIES = Pattern.compile(
            "(?i)\\b(?:amenities|facilities|spa|pool|swimming\\s+pool|fitness|gym|wi[- ]?fi|parking|front\\s+desk|housekeeping|concierge)\\b"
    );
    private static final Pattern CONTACT = Pattern.compile(
            "(?i)\\b(?:contact(?:\\s+us)?|get\\s+in\\s+touch|directions?|location)\\b"
    );
    private static final Pattern CONTACT_NEGATIVE = Pattern.compile(
            "(?i)\\b(?:supplier|vendor|partner|corporate|investor|press)\\b"
    );
    private static final Pattern BOOKING = Pattern.compile(
            "(?i)\\b(?:book(?:\\s+now)?|reserve|reservation|reservations|check\\s+availability|book\\s+your\\s+stay)\\b"
    );
    private static final Pattern BOOKING_HREF = Pattern.compile(
            "(?i)(?:/|\\b)(?:book|booking|reserve|reservation|availability)(?:/|\\b)"
    );
    private static final Pattern DINING = Pattern.compile(
            "(?i)\\b(?:restaurant|dining|breakfast|bar|cafe|café)\\b"
    );
    private static final Pattern DINING_HREF = Pattern.compile(
            "(?i)(?:/|\\b)(?:dining|restaurant|breakfast)(?:/|\\b)"
    );

    public List<HospitalityObservation> observe(StructuredEvidence evidence) {
        Objects.requireNonNull(evidence, "evidence must not be null");

        if (evidence.sourceProvenance() != EvidenceProvenance.DISCOVERED
                || !evidence.representsSuccessfulObservation()
                || !evidence.hasObservedContent()
                || evidence.observedContent().isBlank()) {
            return List.of();
        }

        List<HospitalityObservation> observations = new ArrayList<>();
        Set<String> emitted = new LinkedHashSet<>();
        Matcher matcher = SEMANTIC_ELEMENT.matcher(evidence.observedContent());

        while (matcher.find()) {
            String tag = matcher.group(1).toLowerCase(Locale.ROOT);
            String attributes = matcher.group(2);
            String text = normalizeText(matcher.group(3));
            if (text.isEmpty() && !tag.equals("a")) {
                continue;
            }

            String href = extractHref(attributes);
            String sourceReference = sourceReference(evidence, tag, href);

            addObservation(observations, emitted, evidence, HospitalityObservationCategory.HOTEL_IDENTITY,
                    hotelIdentityValue(tag, text), sourceReference);

            if (isRoomSignal(text, href)) {
                addObservation(observations, emitted, evidence, HospitalityObservationCategory.ROOMS,
                        value(text, href), sourceReference);
            }
            if (isAmenitySignal(text)) {
                addObservation(observations, emitted, evidence, HospitalityObservationCategory.AMENITIES,
                        value(text, href), sourceReference);
            }
            if (isContactSignal(tag, text, href)) {
                addObservation(observations, emitted, evidence, HospitalityObservationCategory.CONTACT,
                        value(text, href), sourceReference);
            }
            if (isBookingSignal(text, href)) {
                addObservation(observations, emitted, evidence, HospitalityObservationCategory.BOOKING,
                        value(text, href), sourceReference);
            }
            if (isDiningSignal(text, href)) {
                addObservation(observations, emitted, evidence, HospitalityObservationCategory.DINING,
                        value(text, href), sourceReference);
            }
        }

        return List.copyOf(observations);
    }

    private static String hotelIdentityValue(String tag, String text) {
        return (tag.equals("title") || tag.matches("h[1-3]")) && HOTEL_IDENTITY.matcher(text).find()
                ? text
                : null;
    }

    private static boolean isRoomSignal(String text, String href) {
        if (ROOM_NEGATIVE.matcher(text).find()) {
            return false;
        }
        return ROOMS.matcher(text).find() || ROOMS.matcher(nullToEmpty(href)).find();
    }

    private static boolean isAmenitySignal(String text) {
        return AMENITIES.matcher(text).find();
    }

    private static boolean isContactSignal(String tag, String text, String href) {
        String normalizedHref = nullToEmpty(href).toLowerCase(Locale.ROOT);
        if (normalizedHref.startsWith("mailto:") || normalizedHref.startsWith("tel:")) {
            return true;
        }
        if (!tag.equals("title") && CONTACT.matcher(text).find()) {
            return !CONTACT_NEGATIVE.matcher(text).find();
        }
        return false;
    }

    private static boolean isBookingSignal(String text, String href) {
        return BOOKING.matcher(text).find() || BOOKING_HREF.matcher(nullToEmpty(href)).find();
    }

    private static boolean isDiningSignal(String text, String href) {
        return DINING.matcher(text).find() || DINING_HREF.matcher(nullToEmpty(href)).find();
    }

    private static void addObservation(
            List<HospitalityObservation> observations,
            Set<String> emitted,
            StructuredEvidence evidence,
            HospitalityObservationCategory category,
            String observedValue,
            String sourceReference
    ) {
        if (observedValue == null || observedValue.isBlank()) {
            return;
        }
        String key = category.name() + "|" + observedValue + "|" + sourceReference;
        if (!emitted.add(key)) {
            return;
        }
        observations.add(new HospitalityObservation(
                category,
                observedValue,
                sourceReference,
                evidence,
                evidence.evaluationId(),
                evidence.attemptId(),
                evidence.attemptNumber(),
                EvidenceProvenance.DISCOVERED
        ));
    }

    private static String value(String text, String href) {
        if (!nullToEmpty(href).isBlank()) {
            return text.isBlank() ? href : text + " -> " + href;
        }
        return text;
    }

    private static String sourceReference(StructuredEvidence evidence, String tag, String href) {
        String base = evidence.finalUrl() != null ? evidence.finalUrl() : evidence.requestedUrl();
        if (!nullToEmpty(href).isBlank()) {
            return base + " [" + tag + " href=" + href + "]";
        }
        return base + " [" + tag + "]";
    }

    private static String extractHref(String attributes) {
        Matcher matcher = HREF.matcher(attributes);
        return matcher.find() ? matcher.group(2).trim() : "";
    }

    private static String normalizeText(String html) {
        String text = html.replaceAll("(?is)<[^>]+>", " ");
        text = text.replace("&amp;", "&")
                .replace("&nbsp;", " ")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&lt;", "<")
                .replace("&gt;", ">");
        return text.replaceAll("\\s+", " ").trim();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
