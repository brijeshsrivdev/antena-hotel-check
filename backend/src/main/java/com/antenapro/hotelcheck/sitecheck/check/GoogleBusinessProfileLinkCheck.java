package com.antenapro.hotelcheck.sitecheck.check;

import com.antenapro.hotelcheck.sitecheck.html.HtmlSupport;
import com.antenapro.hotelcheck.sitecheck.model.FetchedPage;
import com.antenapro.hotelcheck.sitecheck.model.Finding;
import com.antenapro.hotelcheck.sitecheck.model.SiteSnapshot;
import com.antenapro.hotelcheck.sitecheck.model.Stage;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/** PASS: a Google Maps / Business Profile link or an embedded map on any fetched page. */
class GoogleBusinessProfileLinkCheck extends AbstractSiteCheck {

    GoogleBusinessProfileLinkCheck() {
        super("gbp-link", "Google Business Profile link", Stage.TRUST,
                "Linking the site and the Business Profile both ways is how Google connects the website to the local pack listing.", false);
    }

    @Override
    protected Finding check(SiteSnapshot snapshot, Document home) {
        for (FetchedPage page : snapshot.htmlPages()) {
            Element link = page.document().selectFirst(
                    "a[href*=maps.google.], a[href*=google.com/maps], a[href*=google.co.in/maps], a[href*=goo.gl/maps], a[href*=maps.app.goo.gl], a[href*=g.page/], a[href*=g.co/kgs]");
            if (link != null) {
                return pass("Link on the " + page.role().name().toLowerCase() + " page: " + HtmlSupport.describe(link), "Nothing to do.");
            }
            Element embed = page.document().selectFirst("iframe[src*=google.com/maps], iframe[src*=maps.google.]");
            if (embed != null) {
                return pass("Embedded Google Map on the " + page.role().name().toLowerCase() + " page (" + HtmlSupport.shorten(embed.attr("src"), 80) + ").", "Nothing to do.");
            }
        }
        return fail("No Google Maps or Business Profile link on the " + snapshot.htmlPages().size() + " page(s) read.",
                "Link your Google Business Profile (the \"Share\" link in Google Maps) from the contact section, and put this website's address in the profile.");
    }
}
