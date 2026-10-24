package com.antenapro.hotelcheck.sitecheck.check;

import com.antenapro.hotelcheck.sitecheck.model.Finding;
import com.antenapro.hotelcheck.sitecheck.model.PsiResult;
import com.antenapro.hotelcheck.sitecheck.model.SiteSnapshot;
import com.antenapro.hotelcheck.sitecheck.model.Stage;
import org.jsoup.nodes.Document;

import java.util.Optional;

/** PASS: the home page transfers at most 3 MB in at most 60 requests, as measured by PageSpeed Insights. */
class PageWeightCheck extends AbstractSiteCheck {

    private static final long MAX_BYTES = 3L * 1024 * 1024;
    private static final int MAX_REQUESTS = 60;

    PageWeightCheck() {
        super("page-weight", "Page weight", Stage.DISCOVER,
                "Heavy pages are slow on the mobile connections most guests use, which feeds straight into Core Web Vitals.", false);
    }

    @Override
    protected Finding check(SiteSnapshot snapshot, Document home) {
        Optional<PsiResult> psi = snapshot.psiResult();
        if (psi.isEmpty() || !psi.get().available() || psi.get().totalBytes() == null) {
            String why = psi.map(p -> p.available() ? "PageSpeed Insights returned no total-byte-weight audit." : p.limitation())
                    .orElse("PageSpeed Insights was not consulted.");
            return unknown(why + " The HTML alone was " + (snapshot.home().bytes() / 1024) + " KB.", "Run the check again once PageSpeed Insights is available.");
        }
        PsiResult p = psi.get();
        long bytes = p.totalBytes();
        Integer requests = p.requestCount();
        String evidence = String.format("Home page transfers %.1f MB in %s requests (PageSpeed Insights, mobile).", bytes / 1048576.0,
                requests == null ? "an unknown number of" : requests.toString());
        boolean bytesOk = bytes <= MAX_BYTES;
        boolean requestsOk = requests == null || requests <= MAX_REQUESTS;
        if (bytesOk && requestsOk) {
            return pass(evidence, "Nothing to do.");
        }
        String fix = "Resize and compress photos (WebP, ≤ 200 KB each), lazy-load those below the fold and drop unused scripts; aim for under 3 MB and 60 requests.";
        boolean farOff = bytes > 2 * MAX_BYTES || (requests != null && requests > 100);
        return farOff ? fail(evidence, fix) : partial(evidence, fix);
    }
}
