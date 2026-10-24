package com.antenapro.hotelcheck.sitecheck.check;

import java.util.List;

/**
 * The catalogue in report order. Robots, sitemap, mobile-friendliness, Core Web Vitals, page weight and
 * broken-link sampling arrive with slice S2 (Google signals); everything here reads the fetched pages only.
 */
public final class SiteChecks {

    private SiteChecks() {
    }

    public static List<SiteCheck> all() {
        return List.of(
                new HttpsCheck(),
                new TitleTagCheck(),
                new MetaDescriptionCheck(),
                new CanonicalCheck(),
                new IndexableCheck(),
                new SingleH1Check(),
                new HotelNameConsistencyCheck(),
                new RoomsPageCheck(),
                new ContentDepthCheck(),
                new LanguageCheck(),
                new InternalLinksCheck(),
                new ImageAltCheck(),
                new StructuredDataCheck(),
                new RatingHonestyCheck(),
                new AddressPhoneVisibleCheck(),
                new GoogleBusinessProfileLinkCheck(),
                new LastUpdatedCheck(),
                new BookingCtaCheck(),
                new OwnBookingEngineCheck(),
                new TapTargetsCheck(),
                new BookingPageReachableCheck());
    }
}
