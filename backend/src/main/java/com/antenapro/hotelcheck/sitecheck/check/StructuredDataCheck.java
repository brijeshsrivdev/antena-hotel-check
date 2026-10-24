package com.antenapro.hotelcheck.sitecheck.check;

import com.antenapro.hotelcheck.sitecheck.html.HtmlSupport;
import com.antenapro.hotelcheck.sitecheck.html.JsonLd;
import com.antenapro.hotelcheck.sitecheck.model.Finding;
import com.antenapro.hotelcheck.sitecheck.model.SiteSnapshot;
import com.antenapro.hotelcheck.sitecheck.model.Stage;
import com.fasterxml.jackson.databind.JsonNode;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * PASS: JSON-LD Hotel/LodgingBusiness/Resort with name, a full PostalAddress, telephone, image and a url on
 * the checked host.
 */
class StructuredDataCheck extends AbstractSiteCheck {

    StructuredDataCheck() {
        super("structured-data", "Structured data (Hotel)", Stage.TRUST,
                "Hotel JSON-LD makes the site eligible for rich results and ties it to the Business Profile.", true);
    }

    @Override
    protected Finding check(SiteSnapshot snapshot, Document home) {
        List<JsonNode> objects = JsonLd.objects(home);
        Optional<JsonNode> lodging = objects.stream().filter(JsonLd::isLodging).findFirst();
        if (lodging.isEmpty()) {
            String types = JsonLd.typeSummary(objects);
            return fail("No Hotel/LodgingBusiness JSON-LD on the home page (types found: " + types + ").",
                    "Add a <script type=\"application/ld+json\"> Hotel object with name, address, telephone, image and url.");
        }
        JsonNode node = lodging.get();
        List<String> missing = new ArrayList<>();
        if (JsonLd.string(node, "name").isEmpty()) {
            missing.add("name");
        }
        JsonNode address = node.get("address");
        if (address == null || !address.isObject()) {
            missing.add("address");
        } else {
            for (String f : List.of("streetAddress", "addressLocality", "postalCode", "addressCountry")) {
                if (JsonLd.string(address, f).isEmpty()) {
                    missing.add("address." + f);
                }
            }
        }
        if (JsonLd.string(node, "telephone").isEmpty()) {
            missing.add("telephone");
        }
        if (JsonLd.string(node, "image").isEmpty()) {
            missing.add("image");
        }
        Optional<String> url = JsonLd.string(node, "url");
        if (url.isEmpty()) {
            missing.add("url");
        } else if (!HtmlSupport.host(url.get()).equals(snapshot.host())) {
            missing.add("url (points to " + HtmlSupport.host(url.get()) + ", not " + snapshot.host() + ")");
        }
        String evidence = "@type " + JsonLd.types(node) + ", name \"" + JsonLd.string(node, "name").map(n -> HtmlSupport.shorten(n, 40)).orElse("") + "\""
                + (missing.isEmpty() ? ", all required fields present." : ". Missing or wrong: " + String.join(", ", missing) + ".");
        if (missing.isEmpty()) {
            return pass(evidence, "Nothing to do.");
        }
        return partial(evidence, "Complete the Hotel JSON-LD: " + String.join(", ", missing) + ".");
    }
}
