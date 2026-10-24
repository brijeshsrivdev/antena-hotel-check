package com.antenapro.hotelcheck.sitecheck.fetch;

import com.antenapro.hotelcheck.sitecheck.Snapshots;
import com.antenapro.hotelcheck.sitecheck.model.PageRole;
import com.antenapro.hotelcheck.sitecheck.model.RobotsResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RobotsTxtTest {

    private static final String URL = "https://hotel.example.com/robots.txt";

    private static RobotsResult parse(String body) {
        return RobotsTxt.parse(Snapshots.text(PageRole.ROBOTS, URL, 200, body));
    }

    @Test
    void parse_ShouldReadSitemapLines_AndAllowAll() {
        RobotsResult r = parse(Snapshots.fixture("robots-allow.txt"));
        assertThat(r.present()).isTrue();
        assertThat(r.blocksAll()).isFalse();
        assertThat(r.sitemapUrls()).containsExactly("https://lexisinnbeachresort.com/sitemap.xml");
    }

    @Test
    void parse_ShouldDetectDisallowSlashForEveryone_AndForGooglebotOnly() {
        assertThat(parse(Snapshots.fixture("robots-block.txt")).blocksAll()).isTrue();
        assertThat(parse(Snapshots.fixture("robots-block.txt")).blockedBy()).isEqualTo("User-agent: * / Disallow: /");
        RobotsResult googlebot = parse(Snapshots.fixture("robots-googlebot-block.txt"));
        assertThat(googlebot.blocksAll()).isTrue();
        assertThat(googlebot.blockedBy()).startsWith("User-agent: Googlebot");
    }

    @Test
    void parse_ShouldLetAGooglebotRecordOverrideAStarBlock_AndIgnorePartialDisallows() {
        assertThat(parse("User-agent: *\nDisallow: /\n\nUser-agent: Googlebot\nAllow: /\n").blocksAll()).isFalse();
        assertThat(parse("User-agent: *\nDisallow: /admin/\nDisallow: /wp-login.php # comment\n").blocksAll()).isFalse();
        assertThat(parse("").blocksAll()).isFalse();
    }

    @Test
    void parse_ShouldCarryTheStatusOfAMissingOrFailedFetch() {
        assertThat(RobotsTxt.parse(Snapshots.text(PageRole.ROBOTS, URL, 404, null)).present()).isFalse();
        RobotsResult failed = RobotsTxt.parse(Snapshots.failed(PageRole.ROBOTS, URL));
        assertThat(failed.status()).isZero();
        assertThat(failed.error()).isNotBlank();
    }
}
