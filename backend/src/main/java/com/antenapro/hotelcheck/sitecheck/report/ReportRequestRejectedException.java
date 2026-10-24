package com.antenapro.hotelcheck.sitecheck.report;

/** The visitor's report request cannot be honoured; the message is safe to show and the status says why. */
public class ReportRequestRejectedException extends RuntimeException {

    private final int httpStatus;

    public ReportRequestRejectedException(int httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public int httpStatus() {
        return httpStatus;
    }
}
