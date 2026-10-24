package com.antenapro.hotelcheck.sitecheck;

import com.antenapro.hotelcheck.sitecheck.check.SiteChecks;
import com.antenapro.hotelcheck.sitecheck.fetch.InvalidUrlException;
import com.antenapro.hotelcheck.sitecheck.fetch.SiteSnapshotLoader;
import com.antenapro.hotelcheck.sitecheck.fetch.UrlPolicy;
import com.antenapro.hotelcheck.sitecheck.model.CheckStatus;
import com.antenapro.hotelcheck.sitecheck.model.RunStatus;
import com.antenapro.hotelcheck.sitecheck.model.SiteCheckRun;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SiteCheckServiceTest {

    private static final String HOME = "https://hotel.example.com/";
    private static final String HTML = "<html lang=en><head><title>Hotel Example – Rooms in Diveagar</title></head><body><h1>Hotel Example</h1></body></html>";

    /** A clock the test moves by hand. */
    static final class MutableClock extends Clock {
        private Instant now = Instant.parse("2026-10-24T10:00:00Z");

        void advance(Duration d) {
            now = now.plus(d);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

    /** Runs submitted work on the calling thread, or never when {@code run} is false. */
    static final class InlineExecutor extends AbstractExecutorService {
        private final boolean run;

        InlineExecutor(boolean run) {
            this.run = run;
        }

        @Override
        public void execute(Runnable command) {
            if (run) {
                command.run();
            }
        }

        @Override
        public void shutdown() {
        }

        @Override
        public List<Runnable> shutdownNow() {
            return List.of();
        }

        @Override
        public boolean isShutdown() {
            return false;
        }

        @Override
        public boolean isTerminated() {
            return false;
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) {
            return true;
        }
    }

    private final MutableClock clock = new MutableClock();
    private final SiteCheckProperties properties = SiteCheckProperties.defaults();
    private final UrlPolicy policy = new UrlPolicy(host -> new InetAddress[]{
            InetAddress.getByName(host.matches("[\\d.]+") ? host : "93.184.216.34")});
    private final Snapshots.FakeFetcher fetcher = new Snapshots.FakeFetcher().serve(HOME, HTML);
    private final SiteCheckRunRegistry registry = new SiteCheckRunRegistry(clock);

    private SiteCheckService service(ExecutorService executor) {
        SiteSnapshotLoader loader = new SiteSnapshotLoader(properties, fetcher, policy,
                new Snapshots.FakePsiClient(com.antenapro.hotelcheck.sitecheck.model.PsiResult.unavailable("no key")),
                java.util.concurrent.Executors.newCachedThreadPool(), clock);
        return new SiteCheckService(properties, policy, loader, new SiteAnalyzer(SiteChecks.all()), registry, executor, clock);
    }

    @Test
    void start_ShouldRunTheAnalysis_AndGetShouldReturnItDone() {
        SiteCheckService service = service(new InlineExecutor(true));

        SiteCheckRun run = service.start("hotel.example.com");

        assertThat(run.requestedUrl().toString()).isEqualTo(HOME);
        SiteCheckRun found = service.get(run.id()).orElseThrow();
        assertThat(found.status()).isEqualTo(RunStatus.DONE);
        assertThat(found.result().findings()).hasSize(27);
        assertThat(found.result().finalUrl()).isEqualTo(HOME);
        assertThat(service.get(UUID.randomUUID())).isEmpty();
    }

    @Test
    void start_ShouldRejectAnInvalidUrl_BeforeCountingItAgainstTheHost() {
        SiteCheckService service = service(new InlineExecutor(true));

        assertThatThrownBy(() -> service.start("http://127.0.0.1/")).isInstanceOf(InvalidUrlException.class);
        assertThat(registry.tryReserve("127.0.0.1", 1)).isTrue();
    }

    @Test
    void start_ShouldAllowThreeRunsPerHostPerDay_ThenRefuse_ThenAllowAgainTheNextDay() {
        SiteCheckService service = service(new InlineExecutor(true));

        service.start(HOME);
        service.start("https://www.hotel.example.com/rooms");
        service.start(HOME);
        assertThatThrownBy(() -> service.start(HOME)).isInstanceOf(HostLimitExceededException.class)
                .hasMessageContaining("hotel.example.com").hasMessageContaining("3 times today");

        clock.advance(Duration.ofHours(25));
        assertThat(service.start(HOME)).isNotNull();
    }

    @Test
    void get_ShouldReportARunDoneWithEveryCheckUnknown_OnceItIsOverdue() {
        SiteCheckService service = service(new InlineExecutor(false));
        SiteCheckRun run = service.start(HOME);

        clock.advance(Duration.ofMillis(properties.runTimeoutMs()).plusSeconds(5));
        assertThat(service.get(run.id()).orElseThrow().status()).isEqualTo(RunStatus.RUNNING);

        clock.advance(Duration.ofSeconds(10));
        SiteCheckRun overdue = service.get(run.id()).orElseThrow();
        assertThat(overdue.status()).isEqualTo(RunStatus.DONE);
        assertThat(overdue.result().score()).isNull();
        assertThat(overdue.result().findings()).allSatisfy(f -> assertThat(f.status()).isEqualTo(CheckStatus.UNKNOWN));
        assertThat(overdue.result().limitations()).singleElement().asString().contains("did not answer within 25 seconds");
    }

    @Test
    void registry_ShouldForgetRunsAfterRetention() {
        SiteCheckService service = service(new InlineExecutor(true));
        SiteCheckRun run = service.start(HOME);

        clock.advance(SiteCheckRunRegistry.RETENTION.plusMinutes(1));
        service.start("https://other.example.com/");

        assertThat(service.get(run.id())).isEmpty();
    }
}
