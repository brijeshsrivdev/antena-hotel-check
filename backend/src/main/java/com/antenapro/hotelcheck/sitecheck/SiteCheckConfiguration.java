package com.antenapro.hotelcheck.sitecheck;

import com.antenapro.hotelcheck.acquisition.HttpPublicWebAcquisitionService;
import com.antenapro.hotelcheck.sitecheck.check.SiteChecks;
import com.antenapro.hotelcheck.sitecheck.fetch.AcquisitionPageFetcher;
import com.antenapro.hotelcheck.sitecheck.fetch.PageFetcher;
import com.antenapro.hotelcheck.sitecheck.fetch.SiteSnapshotLoader;
import com.antenapro.hotelcheck.sitecheck.fetch.UrlPolicy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Wires the Hotel Site Check only when {@code hotelcheck.sitecheck.enabled=true}. Every bean of the feature is
 * declared here (none is component-scanned) so an unset flag leaves no trace: no executor, no endpoint, a 404
 * on the path. The pages are fetched through the existing public-web acquisition boundary (REQ-015).
 */
@Configuration
@ConditionalOnProperty(name = "hotelcheck.sitecheck.enabled", havingValue = "true")
@EnableConfigurationProperties(SiteCheckProperties.class)
public class SiteCheckConfiguration {

    @Bean
    UrlPolicy siteCheckUrlPolicy() {
        return new UrlPolicy();
    }

    @Bean
    PageFetcher siteCheckPageFetcher(SiteCheckProperties properties) {
        return new AcquisitionPageFetcher(new HttpPublicWebAcquisitionService(), properties);
    }

    @Bean
    SiteSnapshotLoader siteSnapshotLoader(SiteCheckProperties properties, PageFetcher fetcher, UrlPolicy policy) {
        return new SiteSnapshotLoader(properties, fetcher, policy, Clock.systemUTC());
    }

    @Bean
    SiteAnalyzer siteAnalyzer() {
        return new SiteAnalyzer(SiteChecks.all());
    }

    @Bean
    SiteCheckRunRegistry siteCheckRunRegistry() {
        return new SiteCheckRunRegistry(Clock.systemUTC());
    }

    @Bean(destroyMethod = "shutdownNow")
    ExecutorService siteCheckExecutor() {
        AtomicInteger n = new AtomicInteger();
        return Executors.newFixedThreadPool(2, r -> {
            Thread t = new Thread(r, "sitecheck-" + n.incrementAndGet());
            t.setDaemon(true);
            return t;
        });
    }

    @Bean
    SiteCheckService siteCheckService(SiteCheckProperties properties, UrlPolicy policy, SiteSnapshotLoader loader,
                                      SiteAnalyzer analyzer, SiteCheckRunRegistry registry, ExecutorService siteCheckExecutor) {
        return new SiteCheckService(properties, policy, loader, analyzer, registry, siteCheckExecutor, Clock.systemUTC());
    }
}
