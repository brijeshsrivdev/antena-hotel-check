package com.antenapro.hotelcheck.sitecheck.check;

import com.antenapro.hotelcheck.sitecheck.html.BookingCta;
import com.antenapro.hotelcheck.sitecheck.html.HtmlSupport;
import com.antenapro.hotelcheck.sitecheck.model.FetchedPage;
import com.antenapro.hotelcheck.sitecheck.model.Finding;
import com.antenapro.hotelcheck.sitecheck.model.PageRole;
import com.antenapro.hotelcheck.sitecheck.model.SiteSnapshot;
import com.antenapro.hotelcheck.sitecheck.model.Stage;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;

/** PASS: both a WhatsApp link and a tel: link on the home or contact page. */
class TapTargetsCheck extends AbstractSiteCheck {

    TapTargetsCheck() {
        super("tap-targets", "WhatsApp and phone tap targets", Stage.BOOK,
                "Most Indian guests book by WhatsApp or a call from their phone; a tappable link turns a visit into a conversation.", false);
    }

    @Override
    protected Finding check(SiteSnapshot snapshot, Document home) {
        List<Document> docs = new ArrayList<>();
        docs.add(home);
        snapshot.page(PageRole.CONTACT).filter(FetchedPage::ok).map(FetchedPage::document).ifPresent(docs::add);
        Element wa = null;
        Element tel = null;
        for (Document doc : docs) {
            if (wa == null) {
                wa = doc.select("a[href]").stream().filter(a -> BookingCta.isWhatsApp(a.attr("href"))).findFirst().orElse(null);
            }
            if (tel == null) {
                tel = doc.selectFirst("a[href^=tel:]");
            }
        }
        String evidence = "WhatsApp link " + (wa == null ? "not found" : "found (" + HtmlSupport.shorten(wa.attr("href"), 50) + ")")
                + "; tel: link " + (tel == null ? "not found" : "found (" + tel.attr("href") + ")") + ".";
        if (wa != null && tel != null) {
            return pass(evidence, "Nothing to do.");
        }
        if (wa == null && tel == null) {
            return fail(evidence, "Add a WhatsApp button (https://wa.me/91XXXXXXXXXX) and a tel: link to the header or footer.");
        }
        return partial(evidence, wa == null ? "Add a WhatsApp button linking to https://wa.me/91XXXXXXXXXX with a pre-filled enquiry message."
                : "Add the phone number as <a href=\"tel:+91…\"> so it can be tapped.");
    }
}
