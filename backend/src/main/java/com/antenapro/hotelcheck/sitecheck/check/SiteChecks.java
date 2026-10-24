package com.antenapro.hotelcheck.sitecheck.check;

import java.util.List;

/** The catalogue in report order: 27 checks, docs/plans/hotel-site-check-plan.md §3. */
public final class SiteChecks {

    private SiteChecks() {
    }

    public static List<SiteCheck> all() {
        return List.of(
                new HttpsCheck(),
                new RobotsCheck(),
                new SitemapCheck(),
                new TitleTagCheck(),
                new MetaDescriptionCheck(),
                new CanonicalCheck(),
                new IndexableCheck(),
                new MobileFriendlyCheck(),
                new CoreWebVitalsCheck(),
                new PageWeightCheck(),
                new SingleH1Check(),
                new HotelNameConsistencyCheck(),
                new RoomsPageCheck(),
                new ContentDepthCheck(),
                new LanguageCheck(),
                new InternalLinksCheck(),
                new BrokenLinksCheck(),
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
