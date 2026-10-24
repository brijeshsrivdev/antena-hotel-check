package com.antenapro.hotelcheck.sitecheck.check;

import com.antenapro.hotelcheck.sitecheck.model.Finding;
import com.antenapro.hotelcheck.sitecheck.model.PsiResult;
import com.antenapro.hotelcheck.sitecheck.model.SiteSnapshot;
import com.antenapro.hotelcheck.sitecheck.model.Stage;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** PASS: LCP ≤ 2.5 s, CLS ≤ 0.1, INP ≤ 200 ms from PageSpeed Insights (field data first, lab data otherwise). */
class CoreWebVitalsCheck extends AbstractSiteCheck {

    CoreWebVitalsCheck() {
        super("core-web-vitals", "Core Web Vitals", Stage.DISCOVER,
                "Core Web Vitals are Google's page-experience signal; a slow or jumpy page loses the tie against a faster one.", false);
    }

    @Override
    protected Finding check(SiteSnapshot snapshot, Document home) {
        Optional<PsiResult> psi = snapshot.psiResult();
        if (psi.isEmpty() || !psi.get().available()) {
            return unknown(psi.map(PsiResult::limitation).orElse("PageSpeed Insights was not consulted."), "Run the check again once PageSpeed Insights is available.");
        }
        PsiResult p = psi.get();
        if (p.lcpMs() == null && p.cls() == null && p.inpMs() == null) {
            return unknown("PageSpeed Insights returned no LCP, CLS or INP for this page (too little Chrome traffic for field data and no lab run).",
                    "Run the check again later.");
        }
        List<String> good = new ArrayList<>();
        List<String> middling = new ArrayList<>();
        List<String> poor = new ArrayList<>();
        grade(p.lcpMs() == null ? null : p.lcpMs() / 1000.0, 2.5, 4.0, p.lcpMs() == null ? "" : String.format("LCP %.1f s", p.lcpMs() / 1000.0), good, middling, poor);
        grade(p.cls(), 0.1, 0.25, p.cls() == null ? "" : String.format("CLS %.2f", p.cls()), good, middling, poor);
        grade(p.inpMs() == null ? null : p.inpMs().doubleValue(), 200, 500, p.inpMs() == null ? "" : "INP " + p.inpMs() + " ms", good, middling, poor);
        List<String> all = new ArrayList<>(good);
        all.addAll(middling);
        all.addAll(poor);
        String evidence = "PageSpeed Insights " + p.source() + " data (mobile): " + String.join(", ", all)
                + (p.inpMs() == null ? "; INP not reported" : "") + (p.performanceScore() == null ? "" : "; Lighthouse performance " + p.performanceScore() + "/100") + ".";
        if (poor.isEmpty() && middling.isEmpty()) {
            return pass(evidence, "Nothing to do.");
        }
        String fix = "Compress and lazy-load images, set width/height on images and ads, and defer third-party scripts; aim for LCP ≤ 2.5 s, CLS ≤ 0.1, INP ≤ 200 ms.";
        return poor.isEmpty() ? partial(evidence, fix) : fail(evidence, fix);
    }

    private static void grade(Double value, double goodMax, double poorMin, String label, List<String> good, List<String> middling, List<String> poor) {
        if (value == null) {
            return;
        }
        if (value <= goodMax) {
            good.add(label + " (good)");
        } else if (value <= poorMin) {
            middling.add(label + " (needs improvement)");
        } else {
            poor.add(label + " (poor)");
        }
    }
}
