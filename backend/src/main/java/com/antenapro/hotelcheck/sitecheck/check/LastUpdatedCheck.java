package com.antenapro.hotelcheck.sitecheck.check;

import com.antenapro.hotelcheck.sitecheck.html.HtmlSupport;
import com.antenapro.hotelcheck.sitecheck.model.Finding;
import com.antenapro.hotelcheck.sitecheck.model.SiteSnapshot;
import com.antenapro.hotelcheck.sitecheck.model.Stage;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PASS: the copyright line carries the current year. The sitemap {@code lastmod} alternative arrives with the
 * Google-signals slice; until then a page without a copyright year reports UNKNOWN.
 */
class LastUpdatedCheck extends AbstractSiteCheck {

    private static final Pattern COPYRIGHT = Pattern.compile("(?i)(©|\\(c\\)|copyright)[^\\n]{0,60}?((?:19|20)\\d{2})(?:\\s*[-–]\\s*((?:19|20)\\d{2}))?");

    LastUpdatedCheck() {
        super("last-updated", "Last-updated signal", Stage.TRUST,
                "A stale copyright year or sitemap tells Google and guests the site is unattended.", false);
    }

    @Override
    protected Finding check(SiteSnapshot snapshot, Document home) {
        Element footer = home.selectFirst("footer, #footer, [class~=(?i)footer]");
        String text = HtmlSupport.text(footer != null ? footer : home.body());
        Matcher m = COPYRIGHT.matcher(text);
        if (!m.find()) {
            return unknown("No copyright year on the home page; the sitemap's lastmod is checked once Google signals are added.",
                    "Add a \"© " + HtmlSupport.currentYear() + " <hotel name>\" line to the footer and keep it current.");
        }
        int year = Integer.parseInt(m.group(3) != null ? m.group(3) : m.group(2));
        int now = HtmlSupport.currentYear();
        String evidence = "Footer says \"" + HtmlSupport.shorten(m.group().trim(), 60) + "\" (year " + year + ", now " + now + ").";
        if (year >= now) {
            return pass(evidence, "Nothing to do.");
        }
        if (year == now - 1) {
            return partial(evidence, "Update the copyright year to " + now + " (or generate it from the current date).");
        }
        return fail(evidence, "Update the copyright year to " + now + " and refresh the content; the site looks abandoned.");
    }
}
