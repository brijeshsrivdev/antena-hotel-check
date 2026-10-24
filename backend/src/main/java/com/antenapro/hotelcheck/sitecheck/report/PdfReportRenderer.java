package com.antenapro.hotelcheck.sitecheck.report;

import com.antenapro.hotelcheck.sitecheck.api.SiteCheckResponse;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/** Renders {@link ReportHtml} to PDF bytes with openhtmltopdf; no network, no external fonts. */
public class PdfReportRenderer {

    public byte[] render(SiteCheckResponse run) {
        String html = ReportHtml.render(run);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            new PdfRendererBuilder().useFastMode().withHtmlContent(html, null).toStream(out).run();
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Could not render the site check PDF", e);
        }
    }
}
