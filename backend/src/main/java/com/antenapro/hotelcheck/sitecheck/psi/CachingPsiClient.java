package com.antenapro.hotelcheck.sitecheck.psi;

import com.antenapro.hotelcheck.sitecheck.model.PsiResult;
import com.antenapro.hotelcheck.sitecheck.model.SiteSnapshot;

import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * One PSI result per host per 24 hours (plan §5 S2): a Lighthouse run takes 10–40 s and burns quota, and the
 * three daily runs a host is allowed would otherwise repeat it. Only successful results are cached; a quota
 * or timeout failure is retried on the next run.
 */
public class CachingPsiClient implements PsiClient {

    static final Duration TTL = Duration.ofHours(24);

    private record Entry(Instant at, PsiResult result) {
    }

    private final PsiClient delegate;
    private final Clock clock;
    private final Map<String, Entry> cache = new ConcurrentHashMap<>();

    public CachingPsiClient(PsiClient delegate, Clock clock) {
        this.delegate = delegate;
        this.clock = clock;
    }

    @Override
    public PsiResult analyze(URI url) {
        String host = SiteSnapshot.hostOf(url.toString());
        Entry hit = cache.get(host);
        Instant now = clock.instant();
        if (hit != null && hit.at().plus(TTL).isAfter(now)) {
            return hit.result();
        }
        PsiResult fresh = delegate.analyze(url);
        if (fresh.available()) {
            cache.put(host, new Entry(now, fresh));
        }
        return fresh;
    }
}
