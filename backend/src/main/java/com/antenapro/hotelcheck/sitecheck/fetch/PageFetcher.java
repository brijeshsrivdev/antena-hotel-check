package com.antenapro.hotelcheck.sitecheck.fetch;

import com.antenapro.hotelcheck.sitecheck.model.FetchedPage;
import com.antenapro.hotelcheck.sitecheck.model.PageRole;

import java.net.URI;

/** Fetches one URL. Never throws: a failed fetch is a {@link FetchedPage} with status 0 and an error. */
public interface PageFetcher {

    /** An HTML page: parsed with jsoup. */
    FetchedPage fetch(URI url, PageRole role);

    /** A small text resource (robots.txt, a sitemap): kept as {@code text}, not parsed as HTML. */
    FetchedPage fetchText(URI url, PageRole role);

    /** Status and timing only, for broken-link sampling; the body is discarded. */
    FetchedPage probe(URI url);
}
