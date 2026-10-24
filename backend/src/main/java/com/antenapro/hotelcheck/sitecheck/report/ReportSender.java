package com.antenapro.hotelcheck.sitecheck.report;

/** Delivers the PDF to the address the visitor typed. */
public interface ReportSender {

    /**
     * @param host the checked site's host, for the subject line and file name
     * @return a human-readable note of what happened (logged, returned to the caller for the local tool)
     */
    String send(String email, String host, byte[] pdf);
}
