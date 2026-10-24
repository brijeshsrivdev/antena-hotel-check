package com.antenapro.hotelcheck.sitecheck.check;

import com.antenapro.hotelcheck.sitecheck.model.Finding;
import com.antenapro.hotelcheck.sitecheck.model.SiteSnapshot;
import com.antenapro.hotelcheck.sitecheck.model.Stage;

/** One line of the check catalogue in docs/plans/hotel-site-check-plan.md. Pure: reads the snapshot, returns a finding. */
public interface SiteCheck {

    String id();

    String title();

    Stage stage();

    Finding run(SiteSnapshot snapshot);
}
