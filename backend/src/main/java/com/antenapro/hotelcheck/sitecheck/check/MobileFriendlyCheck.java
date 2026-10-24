package com.antenapro.hotelcheck.sitecheck.check;

import com.antenapro.hotelcheck.sitecheck.model.Finding;
import com.antenapro.hotelcheck.sitecheck.model.PsiResult;
import com.antenapro.hotelcheck.sitecheck.model.SiteSnapshot;
import com.antenapro.hotelcheck.sitecheck.model.Stage;
import org.jsoup.nodes.Document;

import java.util.Optional;

/**
 * PASS: PageSpeed Insights' mobile viewport and content-width audits both pass. Without PSI the viewport meta
 * tag alone decides between FAIL (absent) and PARTIAL (present, overflow not measured).
 */
class MobileFriendlyCheck extends AbstractSiteCheck {

    MobileFriendlyCheck() {
        super("mobile-friendly", "Mobile-friendly", Stage.DISCOVER,
                "Google indexes the mobile version of the site first; a page that does not fit a phone screen ranks as such.", false);
    }

    @Override
    protected Finding check(SiteSnapshot snapshot, Document home) {
        String viewportMeta = home.select("meta[name=viewport]").attr("content").trim();
        Optional<PsiResult> psi = snapshot.psiResult().filter(PsiResult::available);
        if (psi.isEmpty() || psi.get().viewportPass() == null) {
            if (viewportMeta.isEmpty()) {
                return fail("No <meta name=\"viewport\"> on the home page; the page renders at desktop width on phones.",
                        "Add <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"> and use a responsive layout.");
            }
            return partial("<meta name=\"viewport\" content=\"" + viewportMeta + "\"> is present; horizontal overflow was not measured ("
                    + snapshot.psiResult().map(PsiResult::limitation).orElse("PageSpeed Insights not consulted") + ").",
                    "Run the check again with PageSpeed Insights available to confirm the page fits a phone screen.");
        }
        PsiResult p = psi.get();
        boolean viewport = p.viewportPass();
        boolean width = p.contentWidthPass() == null || p.contentWidthPass();
        String evidence = "PageSpeed Insights (mobile): viewport audit " + (viewport ? "passed" : "failed") + ", content-width audit "
                + (p.contentWidthPass() == null ? "not run" : p.contentWidthPass() ? "passed" : "failed") + "; meta viewport \"" + viewportMeta + "\".";
        if (viewport && width) {
            return pass(evidence, "Nothing to do.");
        }
        if (!viewport) {
            return fail(evidence, "Add <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"> and use a responsive layout.");
        }
        return partial(evidence, "Fix the elements wider than the phone screen (large images, fixed-width tables) so the page does not scroll sideways.");
    }
}
