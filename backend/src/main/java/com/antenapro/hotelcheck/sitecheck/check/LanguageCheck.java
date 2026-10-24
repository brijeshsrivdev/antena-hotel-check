package com.antenapro.hotelcheck.sitecheck.check;

import com.antenapro.hotelcheck.sitecheck.model.Finding;
import com.antenapro.hotelcheck.sitecheck.model.SiteSnapshot;
import com.antenapro.hotelcheck.sitecheck.model.Stage;
import org.jsoup.nodes.Document;

/** PASS: {@code <html lang>} is set to a plausible language tag; Devanagari and English are both fine. */
class LanguageCheck extends AbstractSiteCheck {

    LanguageCheck() {
        super("language", "Page language", Stage.UNDERSTAND,
                "The lang attribute tells Google and screen readers which language the page is in, so it is shown for searches in that language.", false);
    }

    @Override
    protected Finding check(SiteSnapshot snapshot, Document home) {
        String lang = home.select("html").attr("lang").trim();
        if (lang.isEmpty()) {
            return fail("<html> has no lang attribute.", "Add lang=\"en\" (or \"mr\", \"hi\") to the <html> tag.");
        }
        if (!lang.matches("^[A-Za-z]{2,3}(-[A-Za-z0-9]{2,8})*$")) {
            return partial("<html lang=\"" + lang + "\"> is not a valid language tag.", "Use a BCP 47 tag such as lang=\"en-IN\" or lang=\"mr\".");
        }
        return pass("<html lang=\"" + lang + "\">.", "Nothing to do.");
    }
}
