package com.antenapro.hotelcheck.acquisition;

import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.*;

class AcquisitionUrlNormalizerTest {

    @Test
    void testValidHttpAndHttpsUrls() {
        var res1 = AcquisitionUrlNormalizer.normalizeAndValidate("https://example.com/hotel", false);
        assertTrue(res1.valid());
        assertEquals("https://example.com/hotel", res1.normalizedUrl());

        var res2 = AcquisitionUrlNormalizer.normalizeAndValidate("HTTP://EXAMPLE.COM:80/rooms#section", false);
        assertTrue(res2.valid());
        assertEquals("http://example.com/rooms", res2.normalizedUrl());
    }

    @Test
    void testUnsupportedSchemes() {
        var res1 = AcquisitionUrlNormalizer.normalizeAndValidate("file:///etc/passwd", false);
        assertFalse(res1.valid());
        assertEquals(AcquisitionOutcome.UNSUPPORTED_SCHEME, res1.outcome());

        var res2 = AcquisitionUrlNormalizer.normalizeAndValidate("ftp://example.com/file", false);
        assertFalse(res2.valid());
        assertEquals(AcquisitionOutcome.UNSUPPORTED_SCHEME, res2.outcome());

        var res3 = AcquisitionUrlNormalizer.normalizeAndValidate("javascript:alert(1)", false);
        assertFalse(res3.valid());
        assertEquals(AcquisitionOutcome.UNSUPPORTED_SCHEME, res3.outcome());
    }

    @Test
    void testInvalidTargetUrls() {
        var res1 = AcquisitionUrlNormalizer.normalizeAndValidate(null, false);
        assertFalse(res1.valid());
        assertEquals(AcquisitionOutcome.INVALID_TARGET, res1.outcome());

        var res2 = AcquisitionUrlNormalizer.normalizeAndValidate("   ", false);
        assertFalse(res2.valid());
        assertEquals(AcquisitionOutcome.INVALID_TARGET, res2.outcome());

        var res3 = AcquisitionUrlNormalizer.normalizeAndValidate("http:// bad target url", false);
        assertFalse(res3.valid());
        assertEquals(AcquisitionOutcome.INVALID_TARGET, res3.outcome());
    }

    @Test
    void testLoopbackAndPrivateAddressRejection() {
        var res1 = AcquisitionUrlNormalizer.normalizeAndValidate("http://127.0.0.1/admin", false);
        assertFalse(res1.valid());
        assertEquals(AcquisitionOutcome.INVALID_TARGET, res1.outcome());

        var res2 = AcquisitionUrlNormalizer.normalizeAndValidate("http://localhost/admin", false);
        assertFalse(res2.valid());
        assertEquals(AcquisitionOutcome.INVALID_TARGET, res2.outcome());

        // When allowLocalhost is true
        var res3 = AcquisitionUrlNormalizer.normalizeAndValidate("http://127.0.0.1:8080/test", true);
        assertTrue(res3.valid());
        assertEquals("http://127.0.0.1:8080/test", res3.normalizedUrl());
    }

    @Test
    void testCloudMetadataAndPrivateSubnetsRejection() {
        // AWS IMDS / Link-local address
        var res1 = AcquisitionUrlNormalizer.normalizeAndValidate("http://169.254.169.254/latest/meta-data/", false);
        assertFalse(res1.valid());
        assertEquals(AcquisitionOutcome.INVALID_TARGET, res1.outcome());

        // RFC 1918 Private networks
        var res2 = AcquisitionUrlNormalizer.normalizeAndValidate("http://10.0.0.1/internal", false);
        assertFalse(res2.valid());
        assertEquals(AcquisitionOutcome.INVALID_TARGET, res2.outcome());

        var res3 = AcquisitionUrlNormalizer.normalizeAndValidate("http://172.16.0.1/internal", false);
        assertFalse(res3.valid());
        assertEquals(AcquisitionOutcome.INVALID_TARGET, res3.outcome());

        var res4 = AcquisitionUrlNormalizer.normalizeAndValidate("http://192.168.1.1/internal", false);
        assertFalse(res4.valid());
        assertEquals(AcquisitionOutcome.INVALID_TARGET, res4.outcome());

        // Any local / 0.0.0.0
        var res5 = AcquisitionUrlNormalizer.normalizeAndValidate("http://0.0.0.0/test", false);
        assertFalse(res5.valid());
        assertEquals(AcquisitionOutcome.INVALID_TARGET, res5.outcome());
    }

    @Test
    void testIsPublicIpAddressHelper() throws UnknownHostException {
        assertTrue(AcquisitionUrlNormalizer.isPublicIpAddress(InetAddress.getByName("8.8.8.8"), false));
        assertFalse(AcquisitionUrlNormalizer.isPublicIpAddress(InetAddress.getByName("127.0.0.1"), false));
        assertFalse(AcquisitionUrlNormalizer.isPublicIpAddress(InetAddress.getByName("10.1.2.3"), false));
        assertFalse(AcquisitionUrlNormalizer.isPublicIpAddress(InetAddress.getByName("169.254.169.254"), false));
        assertFalse(AcquisitionUrlNormalizer.isPublicIpAddress(InetAddress.getByName("192.168.0.1"), false));
    }
}
