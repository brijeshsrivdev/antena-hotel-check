package com.antenapro.hotelcheck.sitecheck.model;

/**
 * One line the owner can act on.
 *
 * @param check         stable id, e.g. {@code booking-cta}
 * @param title         short name shown in the report
 * @param stage         journey stage the check belongs to
 * @param status        outcome
 * @param evidence      what was observed on the site, at most 300 characters, never the page itself
 * @param fix           one sentence telling the owner what to change
 * @param why           why the signal matters for ranking in Google
 * @param antenaHandles true only where an Antena storefront provides this out of the box
 */
public record Finding(String check, String title, Stage stage, CheckStatus status, String evidence, String fix,
                      String why, boolean antenaHandles) {

    public static final int MAX_EVIDENCE_CHARS = 300;

    public Finding {
        if (evidence != null && evidence.length() > MAX_EVIDENCE_CHARS) {
            evidence = evidence.substring(0, MAX_EVIDENCE_CHARS - 1) + "…";
        }
    }
}
