package com.antenapro.hotelcheck.sitecheck.fetch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UrlPolicyTest {

    private static final Map<String, String> DNS = Map.of(
            "example.com", "93.184.216.34",
            "hotel.example.com", "93.184.216.34",
            "intranet.example.com", "10.0.0.5",
            "lexisinn.antenapro.com", "34.100.1.1",
            "sub.lexisinn.antenapro.com", "34.100.1.1",
            "antenapro.com", "34.100.1.1",
            "www.antenapro.com", "34.100.1.1",
            "api.antenapro.com", "34.100.1.1",
            "dev.antenapro.com", "34.100.1.1",
            "metadata.google.internal", "169.254.169.254");

    /** IP literals resolve without DNS; names come from the table above, anything else is unknown. */
    private final UrlPolicy policy = new UrlPolicy(host -> {
        if (host.matches("[\\d.]+") || host.contains(":")) {
            return new InetAddress[]{InetAddress.getByName(host)};
        }
        String ip = DNS.get(host);
        if (ip == null) {
            throw new UnknownHostException(host);
        }
        return new InetAddress[]{InetAddress.getByName(ip)};
    });

    @Test
    void normalize_ShouldAddHttps_LowerCaseTheHost_AndKeepThePath() {
        URI u = policy.normalize("  Hotel.Example.COM/Rooms?x=1#top ");
        assertThat(u.toString()).isEqualTo("https://hotel.example.com/Rooms?x=1");
        assertThat(policy.normalize("http://example.com").toString()).isEqualTo("http://example.com/");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "http://127.0.0.1:8080", "http://169.254.169.254/latest/meta-data", "http://10.1.2.3/", "http://192.168.1.1/",
            "http://172.16.0.1/", "http://100.64.0.1/", "http://0.0.0.0/", "http://[::1]/", "http://[fd00::1]/",
            "http://localhost/", "http://router.local/", "http://metadata.google.internal/", "https://intranet.example.com/"})
    void normalize_ShouldRejectPrivateLoopbackAndLinkLocalTargets(String url) {
        assertThatThrownBy(() -> policy.normalize(url)).isInstanceOf(InvalidUrlException.class)
                .hasMessageContaining("Local or internal");
    }

    @ParameterizedTest
    @ValueSource(strings = {"ftp://example.com/", "file:///etc/passwd", "javascript:alert(1)"})
    void normalize_ShouldRejectNonHttpSchemes(String url) {
        assertThatThrownBy(() -> policy.normalize(url)).isInstanceOf(InvalidUrlException.class);
    }

    @Test
    void normalize_ShouldRejectCredentialsAndNonStandardPorts() {
        assertThatThrownBy(() -> policy.normalize("https://user:pw@example.com/")).hasMessageContaining("user name");
        assertThatThrownBy(() -> policy.normalize("https://example.com:8443/")).hasMessageContaining("standard web ports");
        assertThat(policy.normalize("https://example.com:443/").toString()).isEqualTo("https://example.com/");
    }

    @ParameterizedTest
    @ValueSource(strings = {"https://antenapro.com/", "https://www.antenapro.com/", "https://api.antenapro.com/", "https://dev.antenapro.com/",
            "https://sub.lexisinn.antenapro.com/"})
    void normalize_ShouldRejectAntenasOwnHosts(String url) {
        assertThatThrownBy(() -> policy.normalize(url)).hasMessageContaining("Antena's own site");
    }

    @Test
    void normalize_ShouldAllowATenantStorefrontSubdomain() {
        assertThat(policy.normalize("lexisinn.antenapro.com").getHost()).isEqualTo("lexisinn.antenapro.com");
    }

    @Test
    void normalize_ShouldRejectUnknownHosts_BlankInput_AndOverlongInput() {
        assertThatThrownBy(() -> policy.normalize("https://nosuchhost.example.org/")).hasMessageContaining("could not find a website");
        assertThatThrownBy(() -> policy.normalize("   ")).hasMessageContaining("Enter the address");
        assertThatThrownBy(() -> policy.normalize("https://example.com/" + "a".repeat(3000))).hasMessageContaining("too long");
    }

    @Test
    void validate_ShouldApplyToRedirectTargetsToo() {
        assertThatThrownBy(() -> policy.validate(URI.create("http://127.0.0.1/"))).isInstanceOf(InvalidUrlException.class);
        policy.validate(URI.create("https://example.com/landing"));
    }
}
