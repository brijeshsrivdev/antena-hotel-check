package com.antenapro.hotelcheck.sitecheck.fetch;

/** The visitor's URL is not one the check may fetch; the message is safe to show them. */
public class InvalidUrlException extends RuntimeException {

    public InvalidUrlException(String message) {
        super(message);
    }
}
