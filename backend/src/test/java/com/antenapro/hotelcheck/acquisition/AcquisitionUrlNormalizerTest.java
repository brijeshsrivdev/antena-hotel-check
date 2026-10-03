package com.antenapro.hotelcheck.acquisition;

import org.junit.jupiter.api.Test;

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
}
