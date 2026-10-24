package com.antenapro.hotelcheck.sitecheck;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Settings for the Hotel Site Check ranking-signal analysis (REQ-038, docs/plans/hotel-site-check-plan.md).
 *
 * <p>{@code hotelcheck.sitecheck.enabled} is {@code false} in {@code application.properties} and is switched on
 * only by {@code application-local.properties}: the tool runs on a developer machine. {@code SiteCheckFlagLeakTest}
 * fails the build if the flag name reaches a workflow, Dockerfile or compose file.</p>
 *
 * @param runTimeoutMs  the run is reported DONE after this long, with UNKNOWN for whatever did not finish
 * @param psiKey        Google PageSpeed Insights key; empty means mobile-friendliness, Core Web Vitals and page weight report UNKNOWN
 * @param psiTimeoutMs  a Lighthouse run takes 10–40 s; past this the run reports the PSI checks UNKNOWN and the result is cached when it lands
 * @param maxLinkProbes internal links sampled from the home page for the broken-links check (0 disables sampling)
 * @param reportFrom    sender of the PDF report email
 * @param reportMode    {@code log} writes the PDF to {@code reportOutputDir} and logs the path (local default); {@code smtp} sends it through the configured JavaMailSender
 */
@ConfigurationProperties(prefix = "hotelcheck.sitecheck")
public record SiteCheckProperties(
        boolean enabled,
        String userAgent,
        int connectTimeoutMs,
        int readTimeoutMs,
        int maxPageBytes,
        int maxPages,
        int maxRedirects,
        int runTimeoutMs,
        int maxRunsPerHostPerDay,
        String psiKey,
        int psiTimeoutMs,
        int maxLinkProbes,
        String reportFrom,
        String reportMode,
        String reportOutputDir
) {
    public static final String DEFAULT_USER_AGENT = "AntenaSiteCheck/1.0 (+https://antenapro.com/tools/hotel-site-check)";

    public SiteCheckProperties {
        if (userAgent == null || userAgent.isBlank()) userAgent = DEFAULT_USER_AGENT;
        if (connectTimeoutMs <= 0) connectTimeoutMs = 3_000;
        if (readTimeoutMs <= 0) readTimeoutMs = 8_000;
        if (maxPageBytes <= 0) maxPageBytes = 2 * 1024 * 1024;
        if (maxPages <= 0) maxPages = 5;
        if (maxRedirects < 0) maxRedirects = 5;
        if (runTimeoutMs <= 0) runTimeoutMs = 25_000;
        if (maxRunsPerHostPerDay <= 0) maxRunsPerHostPerDay = 3;
        if (psiKey == null) psiKey = "";
        if (psiTimeoutMs <= 0) psiTimeoutMs = 40_000;
        if (maxLinkProbes < 0) maxLinkProbes = 6;
        if (reportFrom == null || reportFrom.isBlank()) reportFrom = "info@antenapro.com";
        if (reportMode == null || reportMode.isBlank()) reportMode = "log";
        if (reportOutputDir == null || reportOutputDir.isBlank()) reportOutputDir = System.getProperty("java.io.tmpdir") + "/hotelcheck-reports";
    }

    public static SiteCheckProperties defaults() {
        return new SiteCheckProperties(false, null, 0, 0, 0, 0, -1, 0, 0, null, 0, -1, null, null, null);
    }

    public SiteCheckProperties withMaxPages(int pages) {
        return new SiteCheckProperties(enabled, userAgent, connectTimeoutMs, readTimeoutMs, maxPageBytes, pages, maxRedirects,
                runTimeoutMs, maxRunsPerHostPerDay, psiKey, psiTimeoutMs, maxLinkProbes, reportFrom, reportMode, reportOutputDir);
    }

    public SiteCheckProperties withPsiKey(String key) {
        return new SiteCheckProperties(enabled, userAgent, connectTimeoutMs, readTimeoutMs, maxPageBytes, maxPages, maxRedirects,
                runTimeoutMs, maxRunsPerHostPerDay, key, psiTimeoutMs, maxLinkProbes, reportFrom, reportMode, reportOutputDir);
    }

    public SiteCheckProperties withReportOutputDir(String dir) {
        return new SiteCheckProperties(enabled, userAgent, connectTimeoutMs, readTimeoutMs, maxPageBytes, maxPages, maxRedirects,
                runTimeoutMs, maxRunsPerHostPerDay, psiKey, psiTimeoutMs, maxLinkProbes, reportFrom, reportMode, dir);
    }
}
