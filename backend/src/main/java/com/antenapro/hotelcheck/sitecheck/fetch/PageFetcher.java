package com.antenapro.hotelcheck.sitecheck.fetch;

import com.antenapro.hotelcheck.sitecheck.model.FetchedPage;
import com.antenapro.hotelcheck.sitecheck.model.PageRole;

import java.net.URI;

/** Fetches one page. Never throws: a failed fetch is a {@link FetchedPage} with status 0 and an error. */
public interface PageFetcher {

    FetchedPage fetch(URI url, PageRole role);
}
