package com.antenapro.hotelcheck.sitecheck.check;

import com.antenapro.hotelcheck.sitecheck.model.FetchedPage;
import com.antenapro.hotelcheck.sitecheck.model.Finding;
import com.antenapro.hotelcheck.sitecheck.model.PageRole;
import com.antenapro.hotelcheck.sitecheck.model.SiteSnapshot;
import com.antenapro.hotelcheck.sitecheck.model.Stage;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** PASS: no page carries a noindex robots meta tag or X-Robots-Tag header. */
class IndexableCheck extends AbstractSiteCheck {

    IndexableCheck() {
        super("indexable", "Indexable", Stage.DISCOVER,
                "A noindex directive removes the page from Google entirely, whatever else is right.", false);
    }

    @Override
    protected Finding check(SiteSnapshot snapshot, Document home) {
        List<String> blocked = new ArrayList<>();
        for (FetchedPage page : snapshot.htmlPages()) {
            String role = page.role().name().toLowerCase(Locale.ROOT);
            String meta = page.document().select("meta[name=robots], meta[name=googlebot]").stream()
                    .map(m -> m.attr("content").toLowerCase(Locale.ROOT)).filter(c -> c.contains("noindex")).findFirst().orElse("");
            if (!meta.isEmpty()) {
                blocked.add(role + " page: <meta name=\"robots\" content=\"" + meta + "\">");
            }
            String header = page.header("x-robots-tag").map(h -> h.toLowerCase(Locale.ROOT)).filter(h -> h.contains("noindex")).orElse("");
            if (!header.isEmpty()) {
                blocked.add(role + " page: X-Robots-Tag: " + header);
            }
        }
        if (blocked.isEmpty()) {
            return pass("No noindex meta tag or X-Robots-Tag header on the " + snapshot.htmlPages().size() + " page(s) read.", "Nothing to do.");
        }
        boolean homeBlocked = blocked.stream().anyMatch(b -> b.startsWith(PageRole.HOME.name().toLowerCase(Locale.ROOT)));
        String fix = "Remove the noindex directive (in the page's <head> or the server's X-Robots-Tag header) from pages that should rank.";
        return homeBlocked ? fail(String.join("; ", blocked), fix) : partial(String.join("; ", blocked), fix);
    }
}
