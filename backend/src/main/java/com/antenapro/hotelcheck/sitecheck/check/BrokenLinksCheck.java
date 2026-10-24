package com.antenapro.hotelcheck.sitecheck.check;

import com.antenapro.hotelcheck.sitecheck.html.HtmlSupport;
import com.antenapro.hotelcheck.sitecheck.model.FetchedPage;
import com.antenapro.hotelcheck.sitecheck.model.Finding;
import com.antenapro.hotelcheck.sitecheck.model.PageRole;
import com.antenapro.hotelcheck.sitecheck.model.SiteSnapshot;
import com.antenapro.hotelcheck.sitecheck.model.Stage;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;

/** PASS: none of the sampled internal links (the fetched pages plus up to six probes) answers 400 or worse. */
class BrokenLinksCheck extends AbstractSiteCheck {

    BrokenLinksCheck() {
        super("broken-links", "Broken links", Stage.EXPLORE,
                "Links to missing pages waste crawl budget and drop guests on error pages; Google reads them as neglect.", false);
    }

    @Override
    protected Finding check(SiteSnapshot snapshot, Document home) {
        if (snapshot.linkProbes() == null) {
            return unknown("Internal links were not sampled within the time limit.", "Run the check again.");
        }
        List<FetchedPage> sampled = new ArrayList<>();
        snapshot.pages().values().stream()
                .filter(p -> p.role() != PageRole.HOME && !HtmlSupport.samePage(p.finalUrl(), snapshot.home().finalUrl()))
                .forEach(sampled::add);
        sampled.addAll(snapshot.linkProbes());
        if (sampled.isEmpty()) {
            return pass("The home page links to no other page on the site, so there was nothing to sample.", "Nothing to do.");
        }
        List<String> broken = sampled.stream().filter(p -> !p.responded() || p.status() >= 400)
                .map(p -> HtmlSupport.shorten(p.requestedUrl(), 60) + " -> " + p.outcome()).distinct().toList();
        String evidence = sampled.size() + " internal link(s) sampled, " + broken.size() + " broken"
                + (broken.isEmpty() ? "." : ": " + String.join("; ", broken.stream().limit(3).toList()));
        if (broken.isEmpty()) {
            return pass(evidence, "Nothing to do.");
        }
        return fail(evidence, "Fix or remove these links (restore the page or point the link at the right address).");
    }
}
