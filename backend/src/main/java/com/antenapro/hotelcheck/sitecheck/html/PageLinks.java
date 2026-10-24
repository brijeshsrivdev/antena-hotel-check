package com.antenapro.hotelcheck.sitecheck.html;

import com.antenapro.hotelcheck.sitecheck.model.PageRole;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Finds the rooms, contact, booking and about links on a page by href path and anchor text. Used by the
 * snapshot loader to pick the pages it fetches and by the internal-links check to grade the crawl paths.
 */
public final class PageLinks {

    /** A link on the page: the element, its absolute URL and its anchor text. */
    public record Link(Element element, String url, String text) {
        public String describe() {
            return HtmlSupport.describe(element);
        }
    }

    private static final Map<PageRole, Pattern> HREF = new EnumMap<>(Map.of(
            PageRole.ROOMS, Pattern.compile("(?i)/(rooms?|accommodations?|suites?|stay|villas?|cottages?|tariff|tariffs)(/|\\.|$|\\?|#)"),
            PageRole.CONTACT, Pattern.compile("(?i)/(contact|contact-us|contactus|reach-us|location)(/|\\.|$|\\?|#)"),
            PageRole.BOOKING, Pattern.compile("(?i)/(book|booking|bookings|book-now|booknow|reserve|reservation|reservations|availability|check-availability)(/|\\.|$|\\?|#)"),
            PageRole.ABOUT, Pattern.compile("(?i)/(about|about-us|aboutus|our-story|the-resort|the-hotel)(/|\\.|$|\\?|#)")));

    private static final Map<PageRole, Pattern> TEXT = new EnumMap<>(Map.of(
            PageRole.ROOMS, Pattern.compile("(?i)\\b(rooms?|accommodations?|suites?|villas?|cottages?|tariff|where to stay)\\b"),
            PageRole.CONTACT, Pattern.compile("(?i)\\b(contact|reach us|get in touch|location)\\b"),
            PageRole.BOOKING, Pattern.compile("(?i)\\b(book|booking|reserve|reservation|availability)\\b"),
            PageRole.ABOUT, Pattern.compile("(?i)\\b(about|our story)\\b")));

    private static final Set<String> VAGUE_ANCHORS = Set.of("click here", "here", "read more", "more", "learn more", "link", "go", "view");

    private PageLinks() {
    }

    /** Same-site links on the page, in document order, with absolute URLs; mailto/tel/javascript links excluded. */
    public static List<Link> internalLinks(Document document) {
        String base = document.location();
        return document.select("a[href]").stream()
                .map(a -> new Link(a, a.absUrl("href"), HtmlSupport.text(a).isEmpty() ? a.attr("aria-label").trim() : HtmlSupport.text(a)))
                .filter(l -> l.url().startsWith("http") && HtmlSupport.sameSite(l.url(), base))
                .toList();
    }

    /** The first same-site link that looks like the given page, by href path first and anchor text second. */
    public static Optional<Link> find(Document document, PageRole role) {
        List<Link> links = internalLinks(document);
        Pattern href = HREF.get(role);
        Pattern text = TEXT.get(role);
        if (href == null) {
            return Optional.empty();
        }
        Optional<Link> byHref = links.stream().filter(l -> href.matcher(pathOf(l.url())).find()).findFirst();
        if (byHref.isPresent()) {
            return byHref;
        }
        return links.stream().filter(l -> text.matcher(l.text()).find()).findFirst();
    }

    public static boolean isDescriptive(String anchorText) {
        String t = anchorText == null ? "" : anchorText.trim().toLowerCase(Locale.ROOT);
        return t.length() >= 3 && !VAGUE_ANCHORS.contains(t);
    }

    private static String pathOf(String url) {
        try {
            java.net.URI u = java.net.URI.create(url);
            return (u.getPath() == null ? "/" : u.getPath()) + (u.getQuery() == null ? "" : "?" + u.getQuery());
        } catch (IllegalArgumentException e) {
            return url;
        }
    }
}
