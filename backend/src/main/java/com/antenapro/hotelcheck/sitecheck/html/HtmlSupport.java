package com.antenapro.hotelcheck.sitecheck.html;

import com.antenapro.hotelcheck.sitecheck.model.Finding;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.URI;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/** Small, pure helpers shared by the checks. Everything reads jsoup elements; nothing runs regex over raw HTML. */
public final class HtmlSupport {

    private HtmlSupport() {
    }

    /** Element text with whitespace collapsed. */
    public static String text(Element element) {
        return element == null ? "" : element.text().replaceAll("\\s+", " ").trim();
    }

    /** Visible text of the page body: jsoup leaves script and style data out of {@code text()} already. */
    public static String visibleText(Document document) {
        return document.body() == null ? "" : text(document.body());
    }

    public static int wordCount(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        return text.trim().split("\\s+").length;
    }

    /** Evidence is capped by {@link Finding}; this makes the cut visible instead of silent. */
    public static String clip(String s) {
        if (s == null) {
            return "";
        }
        return s.length() > Finding.MAX_EVIDENCE_CHARS ? s.substring(0, Finding.MAX_EVIDENCE_CHARS - 1) + "…" : s;
    }

    /** A short printable form of an element: tag, up to 80 characters of text and the href if any. */
    public static String describe(Element el) {
        String t = text(el);
        if (t.length() > 80) {
            t = t.substring(0, 79) + "…";
        }
        String href = el.hasAttr("href") ? " href=\"" + shorten(el.attr("href"), 80) + "\"" : "";
        return "<" + el.tagName() + href + ">" + t + "</" + el.tagName() + ">";
    }

    public static String shorten(String s, int max) {
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }

    /** Lower-case letters and digits only, for fuzzy name comparison. */
    public static String normalizeName(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{Nd}]+", " ").trim();
    }

    /**
     * 1.0 when one name contains the other, else the Sørensen–Dice coefficient over character bigrams of the
     * normalised names. The plan's threshold for "agree" is 0.8.
     */
    public static double similarity(String a, String b) {
        String x = normalizeName(a);
        String y = normalizeName(b);
        if (x.isEmpty() || y.isEmpty()) {
            return 0;
        }
        if (x.contains(y) || y.contains(x)) {
            return 1.0;
        }
        Set<String> bx = bigrams(x.replace(" ", ""));
        Set<String> by = bigrams(y.replace(" ", ""));
        if (bx.isEmpty() || by.isEmpty()) {
            return x.equals(y) ? 1.0 : 0;
        }
        int common = 0;
        for (String g : bx) {
            if (by.contains(g)) {
                common++;
            }
        }
        return 2.0 * common / (bx.size() + by.size());
    }

    private static Set<String> bigrams(String s) {
        Set<String> out = new HashSet<>();
        for (int i = 0; i + 1 < s.length(); i++) {
            out.add(s.substring(i, i + 2));
        }
        return out;
    }

    /** Host of an absolute URL, lower-case, without {@code www.}; empty when it cannot be parsed. */
    public static String host(String url) {
        if (url == null || url.isBlank()) {
            return "";
        }
        try {
            String h = URI.create(url.trim()).getHost();
            if (h == null) {
                return "";
            }
            h = h.toLowerCase(Locale.ROOT);
            return h.startsWith("www.") ? h.substring(4) : h;
        } catch (IllegalArgumentException e) {
            return "";
        }
    }

    public static boolean sameSite(String urlA, String urlB) {
        String a = host(urlA);
        return !a.isEmpty() && a.equals(host(urlB));
    }

    /** Two URLs name the same page when they match ignoring scheme case, a trailing slash and the fragment. */
    public static boolean samePage(String a, String b) {
        return canonicalForm(a).equals(canonicalForm(b));
    }

    public static String canonicalForm(String url) {
        if (url == null) {
            return "";
        }
        String s = url.trim();
        int hash = s.indexOf('#');
        if (hash >= 0) {
            s = s.substring(0, hash);
        }
        try {
            URI u = URI.create(s);
            String path = u.getRawPath() == null || u.getRawPath().isEmpty() ? "/" : u.getRawPath();
            if (path.length() > 1 && path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
            String query = u.getRawQuery() == null ? "" : "?" + u.getRawQuery();
            return (u.getScheme() == null ? "" : u.getScheme().toLowerCase(Locale.ROOT)) + "://" + host(s) + path + query;
        } catch (IllegalArgumentException e) {
            return s.toLowerCase(Locale.ROOT);
        }
    }

    /** Current year in India, where every site this tool is used on operates. */
    public static int currentYear() {
        return java.time.Year.now(java.time.ZoneId.of("Asia/Kolkata")).getValue();
    }
}
