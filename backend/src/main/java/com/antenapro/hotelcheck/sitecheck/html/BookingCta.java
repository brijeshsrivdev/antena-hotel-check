package com.antenapro.hotelcheck.sitecheck.html;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * The booking call to action: any element — anchor, button, submit input, role=button — whose text or href
 * says book / reserve / check availability / wa.me. Reading every element's text is what the reference
 * repo's anchor-only regex missed on an Antena storefront ({@code <button>Check Availability</button>}).
 */
public final class BookingCta {

    public record Cta(Element element, String text, String href, boolean aboveFold) {
        public String describe() {
            return HtmlSupport.describe(element);
        }

        public boolean isMessagingOrCall() {
            return BookingCta.isWhatsApp(href) || isTel(href);
        }
    }

    private static final Pattern TEXT = Pattern.compile(
            "(?i)\\b(book(ing)?( now| online| a room| your stay| direct)?|reserve( now)?|reservation|check availability|availability|enquire now|enquiry)\\b");
    private static final Pattern HREF = Pattern.compile(
            "(?i)(book|reserv|availability|wa\\.me|api\\.whatsapp\\.com|whatsapp://)");
    private static final Pattern HERO = Pattern.compile("(?i)hero|banner|masthead|jumbotron|splash|intro|header|cover");

    private BookingCta() {
    }

    public static Optional<Cta> find(Document document) {
        List<Cta> all = findAll(document);
        return all.stream().filter(Cta::aboveFold).findFirst().or(() -> all.stream().findFirst());
    }

    public static List<Cta> findAll(Document document) {
        if (document.body() == null) {
            return List.of();
        }
        List<Element> bodyElements = document.body().getAllElements();
        int total = Math.max(1, bodyElements.size());
        return document.body().select("a, button, input[type=submit], input[type=button], [role=button]").stream()
                .map(el -> {
                    String text = HtmlSupport.text(el);
                    if (text.isEmpty()) {
                        text = el.hasAttr("value") ? el.attr("value").trim() : el.attr("aria-label").trim();
                    }
                    String href = el.hasAttr("href") ? el.absUrl("href") : "";
                    if (href.isEmpty() && el.hasAttr("href")) {
                        href = el.attr("href"); // tel:, whatsapp:// and other schemes jsoup does not absolutise
                    }
                    return new Cta(el, text, href, isLikelyAboveFold(el, bodyElements.indexOf(el), total));
                })
                .filter(c -> TEXT.matcher(c.text()).find() || HREF.matcher(c.href()).find())
                .toList();
    }

    public static boolean isWhatsApp(String href) {
        String h = href == null ? "" : href.toLowerCase(Locale.ROOT);
        return h.contains("wa.me/") || h.contains("api.whatsapp.com") || h.startsWith("whatsapp:");
    }

    public static boolean isTel(String href) {
        return href != null && href.toLowerCase(Locale.ROOT).startsWith("tel:");
    }

    /**
     * No layout engine runs here, so "above the fold" is estimated from document order: inside the header,
     * nav or a hero/banner block, or within the first third of the body's elements. The finding says so.
     */
    static boolean isLikelyAboveFold(Element el, int index, int total) {
        for (Element p = el; p != null; p = p.parent()) {
            String tag = p.tagName();
            if (tag.equals("header") || tag.equals("nav")) {
                return true;
            }
            if (tag.equals("footer")) {
                return false;
            }
            if (HERO.matcher(p.className() + " " + p.id()).find()) {
                return true;
            }
        }
        return index >= 0 && index * 3 < total;
    }
}
