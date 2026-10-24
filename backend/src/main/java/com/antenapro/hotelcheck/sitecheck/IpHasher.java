package com.antenapro.hotelcheck.sitecheck;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.UUID;

/**
 * {@code sha256(ip + daily salt)} as the plan's {@code ip_hash}: the salt is the UTC date plus a secret drawn at
 * startup, so the same caller hashes alike within a day and the stored value cannot be reversed to an address.
 */
public final class IpHasher {

    private final Clock clock;
    private final String secret;

    public IpHasher(Clock clock) {
        this(clock, UUID.randomUUID().toString());
    }

    IpHasher(Clock clock, String secret) {
        this.clock = clock;
        this.secret = secret;
    }

    public String hash(String ip) {
        String salt = LocalDate.ofInstant(clock.instant(), ZoneOffset.UTC) + ":" + secret;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(((ip == null ? "" : ip) + "|" + salt).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
