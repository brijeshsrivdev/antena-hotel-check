package com.antenapro.hotelcheck.sitecheck.fetch;

import com.antenapro.hotelcheck.sitecheck.Snapshots;
import com.antenapro.hotelcheck.sitecheck.model.FetchedPage;
import com.antenapro.hotelcheck.sitecheck.model.PageRole;
import com.antenapro.hotelcheck.sitecheck.model.SitemapResult;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class SitemapXmlTest {

    @Test
    void parse_ShouldCountUrlsAndFindTheNewestLastmod() {
        SitemapResult r = SitemapXml.parse(Snapshots.text(PageRole.SITEMAP, "https://lexisinnbeachresort.com/sitemap.xml", 200, Snapshots.fixture("sitemap-urlset.xml")));
        assertThat(r.validXml()).isTrue();
        assertThat(r.isIndex()).isFalse();
        assertThat(r.urlCount()).isEqualTo(3);
        assertThat(r.latestLastmod()).isEqualTo(LocalDate.now(ZoneId.of("Asia/Kolkata")));
    }

    @Test
    void parse_ShouldRecogniseAnIndex_AndExposeItsFirstChild() {
        FetchedPage fetch = Snapshots.text(PageRole.SITEMAP, "https://betelnutresort.in/sitemap.xml", 200, Snapshots.fixture("sitemap-index.xml"));
        SitemapResult r = SitemapXml.parse(fetch);
        assertThat(r.isIndex()).isTrue();
        assertThat(r.childCount()).isEqualTo(2);
        assertThat(r.urlCount()).isZero();
        assertThat(SitemapXml.firstChild(fetch)).contains("https://betelnutresort.in/page-sitemap.xml");
    }

    @Test
    void parse_ShouldFlagHtmlGarbageAndEmptySitemaps() {
        assertThat(SitemapXml.parse(Snapshots.text(PageRole.SITEMAP, "https://x.example.com/sitemap.xml", 200, "<html><body>404</body></html>")).validXml()).isFalse();
        assertThat(SitemapXml.parse(Snapshots.text(PageRole.SITEMAP, "https://x.example.com/sitemap.xml", 200, "not xml at all")).validXml()).isFalse();
        SitemapResult empty = SitemapXml.parse(Snapshots.text(PageRole.SITEMAP, "https://x.example.com/sitemap.xml", 200,
                "<?xml version=\"1.0\"?><urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"></urlset>"));
        assertThat(empty.validXml()).isTrue();
        assertThat(empty.urlCount()).isZero();
        assertThat(SitemapXml.parse(Snapshots.text(PageRole.SITEMAP, "https://x.example.com/sitemap.xml", 404, null)).present()).isFalse();
    }

    @Test
    void parseDate_ShouldAcceptDateAndDateTimeForms() {
        assertThat(SitemapXml.parseDate("2026-10-01")).contains(LocalDate.of(2026, 10, 1));
        assertThat(SitemapXml.parseDate("2026-10-01T08:00:00+05:30")).contains(LocalDate.of(2026, 10, 1));
        assertThat(SitemapXml.parseDate("yesterday")).isEmpty();
    }
}
