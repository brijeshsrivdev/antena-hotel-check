package com.antenapro.hotelcheck.sitecheck.model;

/**
 * What the PageSpeed Insights mobile run said. {@code available=false} carries the reason in {@code limitation}
 * (no key, quota, timeout); every metric is nullable because PSI reports field data only for sites with
 * enough Chrome traffic and lab data only when Lighthouse ran.
 *
 * @param source "field" (Chrome UX Report) or "lab" (Lighthouse), whichever the vitals came from
 */
public record PsiResult(boolean available, String limitation, Boolean viewportPass, Boolean contentWidthPass,
                        Integer lcpMs, Double cls, Integer inpMs, String source, Long totalBytes, Integer requestCount,
                        Integer performanceScore) {

    public static PsiResult unavailable(String limitation) {
        return new PsiResult(false, limitation, null, null, null, null, null, null, null, null, null);
    }
}
