package com.antenapro.hotelcheck.sitecheck.psi;

import com.antenapro.hotelcheck.sitecheck.model.PsiResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Reads the parts of a PageSpeed Insights v5 response the checks use. Field data (Chrome UX Report,
 * {@code loadingExperience.metrics}) wins over lab data ({@code lighthouseResult.audits}) because it is what
 * Google's page-experience signal uses; lab values fill in when the site has too little traffic for field data.
 * Metric key names follow the current v5 schema; every read is null-safe so a schema drift degrades to UNKNOWN.
 */
public final class PsiResponseParser {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private PsiResponseParser() {
    }

    public static PsiResult parse(String json) {
        JsonNode root;
        try {
            root = MAPPER.readTree(json);
        } catch (Exception e) {
            return PsiResult.unavailable("PageSpeed Insights returned something that is not JSON.");
        }
        if (root == null || root.isNull() || root.isMissingNode()) {
            return PsiResult.unavailable("PageSpeed Insights returned an empty response.");
        }
        if (root.has("error")) {
            JsonNode err = root.get("error");
            String message = err.path("message").asText("");
            int code = err.path("code").asInt(0);
            if (code == 429 || message.toLowerCase().contains("quota")) {
                return PsiResult.unavailable("PageSpeed Insights quota exceeded for this key; mobile-friendliness, Core Web Vitals and page weight are unknown.");
            }
            if (code == 400 || code == 403) {
                return PsiResult.unavailable("PageSpeed Insights rejected the request (" + code + "): " + shorten(message) + ".");
            }
            return PsiResult.unavailable("PageSpeed Insights error " + code + ": " + shorten(message) + ".");
        }
        JsonNode field = root.path("loadingExperience").path("metrics");
        JsonNode audits = root.path("lighthouseResult").path("audits");

        Integer lcp = intMetric(field, "LARGEST_CONTENTFUL_PAINT_MS");
        Double cls = field.path("CUMULATIVE_LAYOUT_SHIFT_SCORE").hasNonNull("percentile")
                ? field.path("CUMULATIVE_LAYOUT_SHIFT_SCORE").get("percentile").asDouble() / 100.0 : null;
        Integer inp = intMetric(field, "INTERACTION_TO_NEXT_PAINT");
        String source = lcp != null || cls != null || inp != null ? "field" : null;
        if (lcp == null && audits.path("largest-contentful-paint").hasNonNull("numericValue")) {
            lcp = (int) Math.round(audits.path("largest-contentful-paint").get("numericValue").asDouble());
            source = source == null ? "lab" : source;
        }
        if (cls == null && audits.path("cumulative-layout-shift").hasNonNull("numericValue")) {
            cls = audits.path("cumulative-layout-shift").get("numericValue").asDouble();
            source = source == null ? "lab" : source;
        }
        Boolean viewport = score(audits, "viewport");
        Boolean contentWidth = score(audits, "content-width");
        Long bytes = audits.path("total-byte-weight").hasNonNull("numericValue")
                ? Math.round(audits.path("total-byte-weight").get("numericValue").asDouble()) : null;
        JsonNode requests = audits.path("network-requests").path("details").path("items");
        Integer requestCount = requests.isArray() ? requests.size() : null;
        JsonNode perfNode = root.path("lighthouseResult").path("categories").path("performance");
        Integer perf = perfNode.hasNonNull("score") ? (int) Math.round(perfNode.get("score").asDouble() * 100) : null;
        if (viewport == null && lcp == null && cls == null && inp == null && bytes == null) {
            return PsiResult.unavailable("PageSpeed Insights answered without the audits this check reads (no Lighthouse run and no field data).");
        }
        return new PsiResult(true, null, viewport, contentWidth, lcp, cls, inp, source, bytes, requestCount, perf);
    }

    private static Integer intMetric(JsonNode metrics, String key) {
        JsonNode m = metrics.path(key);
        return m.hasNonNull("percentile") ? m.get("percentile").asInt() : null;
    }

    /** Lighthouse binary audits: score 1 = pass, 0 = fail, null = not run or not applicable. */
    private static Boolean score(JsonNode audits, String id) {
        JsonNode a = audits.path(id);
        return a.hasNonNull("score") ? a.get("score").asDouble() >= 0.9 : null;
    }

    private static String shorten(String s) {
        return s.length() > 120 ? s.substring(0, 119) + "…" : s;
    }
}
