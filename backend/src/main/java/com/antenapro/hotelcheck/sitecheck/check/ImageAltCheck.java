package com.antenapro.hotelcheck.sitecheck.check;

import com.antenapro.hotelcheck.sitecheck.html.HtmlSupport;
import com.antenapro.hotelcheck.sitecheck.model.Finding;
import com.antenapro.hotelcheck.sitecheck.model.SiteSnapshot;
import com.antenapro.hotelcheck.sitecheck.model.Stage;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.List;

/** PASS: at least 80 % of the home page's images carry a non-empty alt attribute (1×1 tracking pixels ignored). */
class ImageAltCheck extends AbstractSiteCheck {

    ImageAltCheck() {
        super("image-alt", "Image alt text", Stage.EXPLORE,
                "Alt text is how room and view photos appear in Google Images and how screen readers describe them.", false);
    }

    @Override
    protected Finding check(SiteSnapshot snapshot, Document home) {
        List<Element> images = home.select("img").stream().filter(img -> !isPixel(img)).toList();
        if (images.isEmpty()) {
            return pass("No images on the home page.", "Nothing to do.");
        }
        List<Element> missing = images.stream().filter(img -> img.attr("alt").trim().isEmpty()).toList();
        int withAlt = images.size() - missing.size();
        int pct = withAlt * 100 / images.size();
        String examples = missing.stream().limit(3).map(i -> HtmlSupport.shorten(i.attr("src"), 50)).toList().toString();
        String evidence = withAlt + " of " + images.size() + " images have alt text (" + pct + "%)." + (missing.isEmpty() ? "" : " Without: " + examples);
        if (pct >= 80) {
            return pass(evidence, "Nothing to do.");
        }
        String fix = "Add an alt attribute describing each photo, e.g. alt=\"Sea-view deluxe room at Sea Breeze Resort, Diveagar\".";
        return pct >= 50 ? partial(evidence, fix) : fail(evidence, fix);
    }

    private static boolean isPixel(Element img) {
        return "1".equals(img.attr("width")) && "1".equals(img.attr("height"));
    }
}
