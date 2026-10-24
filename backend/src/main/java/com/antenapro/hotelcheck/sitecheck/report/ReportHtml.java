package com.antenapro.hotelcheck.sitecheck.report;

import com.antenapro.hotelcheck.sitecheck.api.SiteCheckResponse;
import com.antenapro.hotelcheck.sitecheck.model.CheckStatus;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

/** The report as self-contained XHTML (openhtmltopdf needs well-formed markup): the same content as the page. */
public final class ReportHtml {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm").withZone(ZoneId.of("Asia/Kolkata"));

    private ReportHtml() {
    }

    public static String render(SiteCheckResponse run) {
        String site = run.finalUrl() != null ? run.finalUrl() : run.requestedUrl();
        StringBuilder h = new StringBuilder();
        h.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><meta charset=\"UTF-8\"/><title>Hotel Site Check</title><style>")
                .append("@page { size: A4; margin: 18mm 16mm; } body { font-family: Helvetica, Arial, sans-serif; color: #1c1b18; font-size: 10.5pt; line-height: 1.4; }")
                .append("h1 { font-size: 20pt; margin: 0 0 4pt; } h2 { font-size: 13pt; margin: 16pt 0 6pt; border-bottom: 1px solid #e4e1d8; padding-bottom: 2pt; }")
                .append(".muted { color: #6b6860; } .score { font-size: 32pt; font-weight: bold; } table { width: 100%; border-collapse: collapse; }")
                .append("th, td { text-align: left; padding: 4pt 6pt; border-bottom: 1px solid #e4e1d8; vertical-align: top; } th { background: #f7f6f2; }")
                .append(".f { margin: 0 0 8pt; padding: 6pt 8pt; border: 1px solid #e4e1d8; border-radius: 4pt; page-break-inside: avoid; }")
                .append(".tag { display: inline-block; font-size: 8pt; font-weight: bold; color: #fff; padding: 1pt 5pt; border-radius: 8pt; margin-right: 4pt; }")
                .append(".PASS { background: #1f7a4d; } .PARTIAL { background: #b7791f; } .FAIL { background: #b42318; } .UNKNOWN { background: #6b6860; }")
                .append(".ev { font-family: Courier, monospace; font-size: 8.5pt; color: #444; background: #f7f6f2; padding: 4pt 6pt; margin-top: 4pt; white-space: pre-wrap; word-wrap: break-word; }")
                .append(".lim { border-left: 3px solid #b7791f; padding: 4pt 8pt; background: #fffaf0; }")
                .append("</style></head><body>");
        h.append("<h1>Hotel Site Check</h1><p class=\"muted\">").append(esc(site)).append(" · checked ").append(DATE.format(run.startedAt())).append(" IST</p>");
        h.append("<p><span class=\"score\">").append(run.score() == null ? "–" : run.score()).append("</span> <span class=\"muted\">out of 100 · ")
                .append(count(run, CheckStatus.FAIL)).append(" to fix, ").append(count(run, CheckStatus.PARTIAL)).append(" partly, ")
                .append(count(run, CheckStatus.PASS)).append(" pass");
        if (count(run, CheckStatus.UNKNOWN) > 0) {
            h.append(", ").append(count(run, CheckStatus.UNKNOWN)).append(" unknown (not scored)");
        }
        h.append("</span></p>");

        h.append("<h2>By journey stage</h2><table><tr><th>Stage</th><th>Score</th><th>Pass</th><th>Partly</th><th>Fix</th><th>Unknown</th><th>Weight</th></tr>");
        for (SiteCheckResponse.StageDto s : run.stages()) {
            h.append("<tr><td>").append(esc(s.label())).append("</td><td>").append(s.score() == null ? "–" : s.score()).append("</td><td>").append(s.pass())
                    .append("</td><td>").append(s.partial()).append("</td><td>").append(s.fail()).append("</td><td>").append(s.unknown()).append("</td><td>").append(s.weight()).append("</td></tr>");
        }
        h.append("</table>");

        if (!run.limitations().isEmpty()) {
            h.append("<h2>What we could not check</h2><div class=\"lim\"><ul>");
            for (String l : run.limitations()) {
                h.append("<li>").append(esc(l)).append("</li>");
            }
            h.append("</ul></div>");
        }

        h.append("<h2>Findings, what to fix first</h2>");
        List<SiteCheckResponse.FindingDto> findings = run.findings().stream()
                .sorted(Comparator.comparingInt(f -> f.status().reportOrder())).toList();
        for (SiteCheckResponse.FindingDto f : findings) {
            h.append("<div class=\"f\"><span class=\"tag ").append(f.status().name()).append("\">").append(label(f.status())).append("</span><b>")
                    .append(esc(f.title())).append("</b> <span class=\"muted\">").append(esc(f.stage().label())).append("</span>");
            if (f.antenaHandles() && f.status() != CheckStatus.PASS) {
                h.append(" <span class=\"muted\">· Antena does this for you</span>");
            }
            if (f.status() != CheckStatus.PASS) {
                h.append("<p style=\"margin:4pt 0 0\">").append(esc(f.fix())).append("</p>");
            }
            if (f.why() != null && !f.why().isBlank()) {
                h.append("<p class=\"muted\" style=\"margin:2pt 0 0\">").append(esc(f.why())).append("</p>");
            }
            h.append("<div class=\"ev\">").append(esc(f.evidence())).append("</div></div>");
        }

        h.append("<h2>Pages read</h2><table><tr><th>Page</th><th>Role</th><th>Status</th><th>Time</th></tr>");
        for (SiteCheckResponse.FetchedDto p : run.fetched()) {
            h.append("<tr><td>").append(esc(p.finalUrl() != null ? p.finalUrl() : p.url())).append("</td><td>").append(esc(p.role())).append("</td><td>")
                    .append(esc(p.error() != null ? p.error() : "HTTP " + p.status())).append("</td><td>").append(p.ms()).append(" ms</td></tr>");
        }
        h.append("</table>");
        h.append("<p class=\"muted\" style=\"margin-top:14pt\">Every finding is what we observed on the pages listed above; nothing here claims a booking succeeded or failed. ")
                .append("Report by Antena (antenapro.com): websites and a booking engine for hotels, built so these checks pass out of the box.</p>");
        h.append("</body></html>");
        return h.toString();
    }

    private static long count(SiteCheckResponse run, CheckStatus status) {
        return run.findings().stream().filter(f -> f.status() == status).count();
    }

    private static String label(CheckStatus s) {
        return switch (s) {
            case PASS -> "PASS";
            case PARTIAL -> "PARTLY";
            case FAIL -> "FIX THIS";
            case UNKNOWN -> "UNKNOWN";
        };
    }

    static String esc(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            switch (c) {
                case '&' -> out.append("&amp;");
                case '<' -> out.append("&lt;");
                case '>' -> out.append("&gt;");
                case '"' -> out.append("&quot;");
                default -> {
                    if (c >= 0x20 || c == '\n' || c == '\t') {
                        out.append(c);
                    }
                }
            }
        }
        return out.toString();
    }
}
