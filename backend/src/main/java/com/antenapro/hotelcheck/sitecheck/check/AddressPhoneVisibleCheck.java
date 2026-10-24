package com.antenapro.hotelcheck.sitecheck.check;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** PASS: a postal address in the page text and a tappable tel: link, on the home or contact page. */
class AddressPhoneVisibleCheck extends AbstractSiteCheck {

    private static final Pattern PIN = Pattern.compile("\\b[1-9]\\d{5}\\b");
    private static final Pattern STREET = Pattern.compile("(?i)\\b(road|rd\\.|street|st\\.|lane|marg|nagar|chowk|taluka|tal\\.|dist\\.?|district|village|highway|near|opp\\.?|opposite|beach road|post|p\\.o\\.)\\b");
    private static final Pattern PHONE_TEXT = Pattern.compile("(\\+91[\\s-]?)?[6-9]\\d{4}[\\s-]?\\d{5}\\b|\\b0\\d{2,4}[\\s-]?\\d{6,8}\\b");

    AddressPhoneVisibleCheck() {
        super("address-phone", "Address and phone visible", Stage.TRUST,
                "Google's local pack cross-checks the address and phone shown on the site with the Business Profile.", false);
    }

    @Override
    protected Finding check(SiteSnapshot snapshot, Document home) {
        List<Document> docs = new ArrayList<>();
        docs.add(home);
        snapshot.page(PageRole.CONTACT).filter(FetchedPage::ok).map(FetchedPage::document).ifPresent(docs::add);

        String address = null;
        String tel = null;
        String phoneText = null;
        for (Document doc : docs) {
            if (address == null) {
                address = findAddress(doc);
            }
            if (tel == null) {
                Element a = doc.selectFirst("a[href^=tel:]");
                tel = a == null ? null : a.attr("href");
            }
            if (phoneText == null) {
                Matcher m = PHONE_TEXT.matcher(HtmlSupport.visibleText(doc));
                phoneText = m.find() ? m.group() : null;
            }
        }
        String evidence = "Address " + (address == null ? "not found" : "found: \"" + HtmlSupport.shorten(address, 90) + "\"")
                + "; phone " + (tel != null ? "tappable (" + tel + ")" : phoneText != null ? "shown as text only (" + phoneText + ")" : "not found") + ".";
        if (address != null && tel != null) {
            return pass(evidence, "Nothing to do.");
        }
        if (address == null && tel == null && phoneText == null) {
            return fail(evidence, "Put the full postal address (with PIN code) and the phone number as a tel: link in the footer of every page.");
        }
        return partial(evidence, address == null ? "Add the full postal address with PIN code to the footer, matching the Business Profile exactly."
                : "Wrap the phone number in <a href=\"tel:+91…\"> so it can be tapped on a phone.");
    }

    private static String findAddress(Document doc) {
        Element addr = doc.selectFirst("address");
        if (addr != null && HtmlSupport.text(addr).length() >= 10) {
            return HtmlSupport.text(addr);
        }
        Element itemprop = doc.selectFirst("[itemprop=address], [itemprop=streetAddress]");
        if (itemprop != null && HtmlSupport.text(itemprop).length() >= 10) {
            return HtmlSupport.text(itemprop);
        }
        String text = HtmlSupport.visibleText(doc);
        Matcher pin = PIN.matcher(text);
        while (pin.find()) {
            int from = Math.max(0, pin.start() - 90);
            String window = text.substring(from, Math.min(text.length(), pin.end() + 10));
            if (STREET.matcher(window).find()) {
                return window.trim();
            }
        }
        return null;
    }
}
