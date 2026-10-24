package com.antenapro.hotelcheck.sitecheck.fetch;

import com.antenapro.hotelcheck.sitecheck.model.FetchedPage;
import com.antenapro.hotelcheck.sitecheck.model.RobotsResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Reads robots.txt the way Googlebot does for the one question that matters here: is the whole site
 * disallowed for {@code *} or for Googlebot? A record is a run of {@code User-agent} lines followed by rules;
 * a Googlebot-specific record overrides the {@code *} one.
 */
public final class RobotsTxt {

    private RobotsTxt() {
    }

    public static RobotsResult parse(FetchedPage fetch) {
        if (!fetch.responded()) {
            return new RobotsResult(fetch.requestedUrl(), 0, false, null, List.of(), fetch.error());
        }
        if (fetch.status() < 200 || fetch.status() >= 300 || fetch.text() == null) {
            return new RobotsResult(fetch.finalUrl(), fetch.status(), false, null, List.of(), null);
        }
        List<String> sitemaps = new ArrayList<>();
        boolean starBlocks = false;
        boolean googlebotBlocks = false;
        boolean googlebotSeen = false;
        List<String> agents = new ArrayList<>();
        boolean inRules = false;
        for (String raw : fetch.text().split("\\r?\\n")) {
            String line = raw.indexOf('#') >= 0 ? raw.substring(0, raw.indexOf('#')) : raw;
            line = line.trim();
            int colon = line.indexOf(':');
            if (line.isEmpty() || colon < 0) {
                continue;
            }
            String field = line.substring(0, colon).trim().toLowerCase(Locale.ROOT);
            String value = line.substring(colon + 1).trim();
            boolean all = value.equals("/") || value.equals("/*");
            switch (field) {
                case "user-agent" -> {
                    if (inRules) {
                        agents.clear();
                        inRules = false;
                    }
                    agents.add(value.toLowerCase(Locale.ROOT));
                }
                case "disallow" -> {
                    inRules = true;
                    for (String agent : agents) {
                        if (agent.equals("*")) {
                            starBlocks |= all;
                        } else if (agent.startsWith("googlebot")) {
                            googlebotSeen = true;
                            googlebotBlocks |= all;
                        }
                    }
                }
                case "allow" -> {
                    inRules = true;
                    for (String agent : agents) {
                        if (agent.startsWith("googlebot")) {
                            googlebotSeen = true;
                            if (all) {
                                googlebotBlocks = false;
                            }
                        }
                    }
                }
                case "sitemap" -> sitemaps.add(value);
                default -> inRules = true;
            }
        }
        boolean blocks = googlebotSeen ? googlebotBlocks : starBlocks;
        String blockedBy = blocks ? (googlebotSeen ? "User-agent: Googlebot" : "User-agent: *") + " / Disallow: /" : null;
        return new RobotsResult(fetch.finalUrl(), fetch.status(), blocks, blockedBy, List.copyOf(sitemaps), null);
    }
}
