package com.antenapro.hotelcheck.sitecheck;

/** The same host was checked as often as the daily limit allows. */
public class HostLimitExceededException extends RuntimeException {

    public HostLimitExceededException(String host, int limit) {
        super(host + " has already been checked " + limit + " times today; try again tomorrow.");
    }
}
