package com.antenapro.hotelcheck.sitecheck.model;

/**
 * What a fetch was for. HOME plus the four discovered pages are the plan's five-page budget; ROBOTS, SITEMAP
 * and LINK (a broken-link probe) are the small non-page fetches slice S2 added.
 */
public enum PageRole {
    HOME, ROOMS, CONTACT, BOOKING, ABOUT, ROBOTS, SITEMAP, LINK;

    public boolean isPage() {
        return ordinal() <= ABOUT.ordinal();
    }
}
