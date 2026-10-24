package com.antenapro.hotelcheck.sitecheck.check;

import com.antenapro.hotelcheck.sitecheck.html.HtmlSupport;
import com.antenapro.hotelcheck.sitecheck.model.Finding;
import com.antenapro.hotelcheck.sitecheck.model.SiteSnapshot;
import com.antenapro.hotelcheck.sitecheck.model.Stage;
import org.jsoup.nodes.Document;

/** PASS: the home page has at least 300 words of visible text. */
class ContentDepthCheck extends AbstractSiteCheck {

    ContentDepthCheck() {
        super("content-depth", "Content depth", Stage.UNDERSTAND,
                "Pages with little text are filtered as thin content; Google needs words to understand what the hotel offers and where.", false);
    }

    @Override
    protected Finding check(SiteSnapshot snapshot, Document home) {
        int words = HtmlSupport.wordCount(HtmlSupport.visibleText(home));
        int images = home.select("img").size();
        String evidence = words + " words of visible text and " + images + " images on the home page.";
        if (words >= 300) {
            return pass(evidence, "Nothing to do.");
        }
        if (words >= 120) {
            return partial(evidence, "Add a paragraph each on the location, the rooms and what guests can do nearby, to reach 300+ words.");
        }
        return fail(words < 50 && images > 0 ? evidence + " The page is mostly images." : evidence,
                "Write at least 300 words on the home page about the hotel, its location and its rooms; text inside images does not count.");
    }
}
