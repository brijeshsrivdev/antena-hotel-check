package com.antenapro.hotelcheck.sitecheck;

import com.antenapro.hotelcheck.acquisition.AcquisitionConfig;
import com.antenapro.hotelcheck.acquisition.HttpPublicWebAcquisitionService;
import com.antenapro.hotelcheck.sitecheck.check.SiteChecks;
import com.antenapro.hotelcheck.sitecheck.fetch.AcquisitionPageFetcher;
import com.antenapro.hotelcheck.sitecheck.fetch.SiteSnapshotLoader;
import com.antenapro.hotelcheck.sitecheck.fetch.UrlPolicy;
import com.antenapro.hotelcheck.sitecheck.model.AnalysisResult;
import com.antenapro.hotelcheck.sitecheck.model.CheckStatus;
import com.antenapro.hotelcheck.sitecheck.model.Finding;
import com.antenapro.hotelcheck.sitecheck.psi.CachingPsiClient;
import com.antenapro.hotelcheck.sitecheck.psi.HttpPsiClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.net.InetAddress;
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Slice S5, objective O3: an Antena storefront must never be told its site is unbookable or invisible. Opt-in
 * because it needs a running site: set {@code HOTELCHECK_TENANT_URL} to a tenant storefront (a locally running
 * antena renderer such as {@code http://lexisinn.localhost:3000}, or a live tenant such as
 * {@code https://lexisinnbeachresort.com}) and run
 * {@code mvn test -Dtest=LiveTenantGuardTest -Dsurefire.failIfNoSpecifiedTests=false}. Without the variable the
 * test is skipped, so CI stays offline. {@code HOTELCHECK_SITECHECK_PSI_KEY} is optional: without it the PSI checks
 * are UNKNOWN, which is not a FAIL.
 */
@EnabledIfEnvironmentVariable(named = "HOTELCHECK_TENANT_URL", matches = ".+")
class LiveTenantGuardTest {

    /** The checks the plan names: a storefront must pass or at worst be UNKNOWN on these, never FAIL. */
    private static final List<String> GUARDED = List.of("robots", "sitemap", "structured-data", "booking-cta", "https", "indexable", "canonical", "own-booking-engine");

    @Test
    void antenaStorefront_ShouldNotFailTheGuardedChecks() {
        String url = System.getenv("HOTELCHECK_TENANT_URL");
        String psiKey = System.getenv().getOrDefault("HOTELCHECK_SITECHECK_PSI_KEY", "");
        SiteCheckProperties properties = SiteCheckProperties.defaults().withPsiKey(psiKey);
        // A local renderer answers on localhost with a port, which the public policy rightly refuses; this guard
        // opts in to it (allowLocalhost in the acquisition config, a permissive policy) and nowhere else.
        boolean local = url.contains("localhost") || url.contains("127.0.0.1");
        AcquisitionConfig config = AcquisitionConfig.builder().allowLocalhost(local)
                .connectTimeout(Duration.ofMillis(properties.connectTimeoutMs())).readTimeout(Duration.ofMillis(properties.readTimeoutMs()))
                .maxResponseSizeBytes(properties.maxPageBytes()).maxRedirects(properties.maxRedirects()).userAgent(properties.userAgent()).build();
        UrlPolicy policy = local ? new UrlPolicy(host -> new InetAddress[]{InetAddress.getLoopbackAddress()}) {
            @Override
            public void validate(URI uri) {
                // local renderer: everything on this machine is fair game for the guard
            }
        } : new UrlPolicy();
        SiteSnapshotLoader loader = new SiteSnapshotLoader(properties, new AcquisitionPageFetcher(new HttpPublicWebAcquisitionService(), config), policy,
                new CachingPsiClient(new HttpPsiClient(properties), Clock.systemUTC()), Executors.newCachedThreadPool(), Clock.systemUTC());

        AnalysisResult result = new SiteAnalyzer(SiteChecks.all()).analyze(loader.load(URI.create(url), Instant.now().plusSeconds(20)));

        String report = result.findings().stream().map(f -> f.check() + "=" + f.status() + " [" + f.evidence() + "]").collect(Collectors.joining("\n"));
        List<Finding> failed = result.findings().stream().filter(f -> GUARDED.contains(f.check()) && f.status() == CheckStatus.FAIL).toList();
        assertThat(failed).as("guarded checks must not FAIL on an Antena storefront (%s)\n%s\nlimitations: %s", url, report, result.limitations()).isEmpty();
        assertThat(result.findings().stream().filter(f -> f.check().equals("booking-cta")).findFirst().orElseThrow().status())
                .as("the storefront's booking control must be found\n%s", report).isIn(CheckStatus.PASS, CheckStatus.PARTIAL);
    }
}
