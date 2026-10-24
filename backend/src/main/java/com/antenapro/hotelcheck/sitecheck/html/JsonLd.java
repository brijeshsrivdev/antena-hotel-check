package com.antenapro.hotelcheck.sitecheck.html;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Reads {@code <script type="application/ld+json">} blocks; flattens {@code @graph} and top-level arrays. */
public final class JsonLd {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** schema.org LodgingBusiness and its subtypes that Google's hotel rich results accept. */
    public static final Set<String> LODGING_TYPES = Set.of(
            "Hotel", "LodgingBusiness", "Resort", "Motel", "Hostel", "BedAndBreakfast", "VacationRental", "Campground");

    private JsonLd() {
    }

    public static List<JsonNode> objects(Document document) {
        List<JsonNode> out = new ArrayList<>();
        for (Element script : document.select("script[type=\"application/ld+json\"]")) {
            String data = script.data().trim();
            if (data.isEmpty()) {
                continue;
            }
            try {
                flatten(MAPPER.readTree(data), out);
            } catch (Exception e) {
                // Invalid JSON-LD is itself a finding; the structured-data check reports "none found".
            }
        }
        return out;
    }

    private static void flatten(JsonNode node, List<JsonNode> out) {
        if (node == null) {
            return;
        }
        if (node.isArray()) {
            node.forEach(n -> flatten(n, out));
            return;
        }
        if (node.isObject()) {
            if (node.has("@graph")) {
                flatten(node.get("@graph"), out);
            }
            if (node.has("@type")) {
                out.add(node);
            }
        }
    }

    /** The {@code @type} values of a node (a string or an array of strings). */
    public static List<String> types(JsonNode node) {
        List<String> types = new ArrayList<>();
        JsonNode t = node.get("@type");
        if (t == null) {
            return types;
        }
        if (t.isArray()) {
            t.forEach(x -> types.add(x.asText()));
        } else {
            types.add(t.asText());
        }
        return types;
    }

    public static boolean isLodging(JsonNode node) {
        return types(node).stream().anyMatch(LODGING_TYPES::contains);
    }

    public static Optional<JsonNode> lodging(Document document) {
        return objects(document).stream().filter(JsonLd::isLodging).findFirst();
    }

    /** First non-blank string at {@code field}; a nested object's {@code name}/{@code url} is not unwrapped. */
    public static Optional<String> string(JsonNode node, String field) {
        if (node == null) {
            return Optional.empty();
        }
        JsonNode v = node.get(field);
        if (v == null || v.isNull()) {
            return Optional.empty();
        }
        if (v.isArray()) {
            for (JsonNode x : v) {
                if (x.isTextual() && !x.asText().isBlank()) {
                    return Optional.of(x.asText().trim());
                }
                if (x.isObject() && x.hasNonNull("url")) {
                    return Optional.of(x.get("url").asText().trim());
                }
            }
            return Optional.empty();
        }
        if (v.isObject()) {
            return v.hasNonNull("url") ? Optional.of(v.get("url").asText().trim()) : Optional.empty();
        }
        String s = v.asText().trim();
        return s.isEmpty() ? Optional.empty() : Optional.of(s);
    }

    /** Distinct type names across all objects, for evidence such as "found Organization, WebSite". */
    public static String typeSummary(List<JsonNode> objects) {
        List<String> names = new ArrayList<>();
        for (JsonNode n : objects) {
            for (String t : types(n)) {
                if (!names.contains(t)) {
                    names.add(t);
                }
            }
        }
        return names.isEmpty() ? "none" : String.join(", ", names);
    }
}
