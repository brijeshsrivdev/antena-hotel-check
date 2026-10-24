package com.antenapro.hotelcheck.sitecheck;

import com.antenapro.hotelcheck.sitecheck.api.SiteCheckController;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/** With the flag unset or false no bean of the feature exists, so the public path is a 404 (REQ-038). */
class SiteCheckFlagGateTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(SiteCheckConfiguration.class, SiteCheckController.class);

    @Test
    void featureBeans_ShouldNotExist_WhenTheFlagIsUnset() {
        runner.run(ctx -> {
            assertThat(ctx).doesNotHaveBean(SiteCheckController.class);
            assertThat(ctx).doesNotHaveBean(SiteCheckService.class);
            assertThat(ctx).doesNotHaveBean("siteCheckExecutor");
        });
    }

    @Test
    void featureBeans_ShouldNotExist_WhenTheFlagIsFalse() {
        runner.withPropertyValues("hotelcheck.sitecheck.enabled=false").run(ctx -> {
            assertThat(ctx).doesNotHaveBean(SiteCheckController.class);
            assertThat(ctx).doesNotHaveBean(SiteCheckService.class);
        });
    }

    @Test
    void featureBeans_ShouldExist_WhenTheFlagIsTrue() {
        runner.withPropertyValues("hotelcheck.sitecheck.enabled=true", "hotelcheck.sitecheck.max-pages=4").run(ctx -> {
            assertThat(ctx).hasSingleBean(SiteCheckController.class);
            assertThat(ctx).hasSingleBean(SiteCheckService.class);
            assertThat(ctx.getBean(SiteCheckProperties.class).maxPages()).isEqualTo(4);
            assertThat(ctx.getBean(SiteCheckProperties.class).userAgent()).startsWith("AntenaSiteCheck/1.0");
        });
    }
}
