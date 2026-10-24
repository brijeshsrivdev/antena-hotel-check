package com.antenapro.hotelcheck.sitecheck.check;

import com.antenapro.hotelcheck.sitecheck.html.HtmlSupport;
import com.antenapro.hotelcheck.sitecheck.html.JsonLd;
import com.antenapro.hotelcheck.sitecheck.model.Finding;
import com.antenapro.hotelcheck.sitecheck.model.SiteSnapshot;
import com.antenapro.hotelcheck.sitecheck.model.Stage;
import com.fasterxml.jackson.databind.JsonNode;
import org.jsoup.nodes.Document;

import java.util.Optional;

/**
 * PASS: no aggregateRating markup (nothing to verify), or one with a review count of at least 1 and a
 * visible review block on the page. A rating in the markup with nothing visible is what Google issues
 * manual actions for.
 */
class RatingHonestyCheck extends AbstractSiteCheck {

    RatingHonestyCheck() {
        super("rating-honesty", "Rating markup is honest", Stage.TRUST,
                "Google penalises review markup that is not backed by reviews visible on the page.", true);
    }

    @Override
    protected Finding check(SiteSnapshot snapshot, Document home) {
        Optional<JsonNode> rating = JsonLd.objects(home).stream()
                .map(n -> n.get("aggregateRating")).filter(r -> r != null && r.isObject()).findFirst();
        if (rating.isEmpty()) {
            return pass("No aggregateRating markup on the home page, so there is nothing to verify.", "Nothing to do.");
        }
        JsonNode r = rating.get();
        int count = Math.max(intOf(r, "reviewCount"), intOf(r, "ratingCount"));
        String value = JsonLd.string(r, "ratingValue").orElse("?");
        boolean visible = home.select("[itemprop=review], [itemprop=reviewBody]").size() > 0
                || home.select("[class~=(?i)review|testimonial|rating], [id~=(?i)review|testimonial|rating]").stream()
                .anyMatch(e -> HtmlSupport.wordCount(HtmlSupport.text(e)) >= 5);
        String evidence = "aggregateRating ratingValue " + value + ", reviewCount " + (count > 0 ? count : "missing")
                + "; visible review block " + (visible ? "found" : "not found") + ".";
        if (count >= 1 && visible) {
            return pass(evidence, "Nothing to do.");
        }
        if (count < 1 && !visible) {
            return fail(evidence, "Either show the guest reviews behind the rating on the page with their count, or remove the aggregateRating markup.");
        }
        return partial(evidence, count < 1 ? "Add reviewCount to the aggregateRating so it states how many reviews back the score."
                : "Show the reviews the rating is based on somewhere on the page, or remove the markup.");
    }

    private static int intOf(JsonNode node, String field) {
        JsonNode v = node.get(field);
        if (v == null || v.isNull()) {
            return 0;
        }
        try {
            return (int) Double.parseDouble(v.asText().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
