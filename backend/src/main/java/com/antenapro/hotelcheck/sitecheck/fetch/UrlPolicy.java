package com.antenapro.hotelcheck.sitecheck.fetch;

import com.antenapro.hotelcheck.acquisition.AcquisitionUrlNormalizer;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.Set;

/**
 * Decides which URLs the check may fetch. Builds on the acquisition boundary's
 * {@link AcquisitionUrlNormalizer} (scheme, syntax, non-public address rejection; the same rules are applied to
 * every redirect hop inside {@code HttpPublicWebAcquisitionService}) and adds what the visitor-facing tool needs:
 * <ul>
 *   <li>a bare host gets {@code https://};</li>
 *   <li>no credentials, default ports only;</li>
 *   <li>every address the host resolves to must be public, checked through an injectable resolver so tests
 *       stay deterministic ({@code 127.0.0.1} and {@code 169.254.169.254} are rejected before any socket opens);</li>
 *   <li>none of Antena's own hosts: the {@code antenapro.com} / {@code antena.site} apex, {@code www} and the
 *       reserved subdomains; tenant storefront subdomains stay allowed.</li>
 * </ul>
 */
public class UrlPolicy {

    /** Resolves a host name to its addresses; replaced in tests. */
    @FunctionalInterface
    public interface Resolver {
        InetAddress[] resolve(String host) throws UnknownHostException;
    }

    private static final int MAX_LENGTH = 2048;
    private static final Set<String> OWN_DOMAINS = Set.of("antenapro.com", "antena.site");
    /** Antena's own subdomains on those domains; everything else there is a tenant storefront. */
    private static final Set<String> RESERVED_SUBDOMAINS = Set.of(
            "www", "app", "api", "admin", "console", "dashboard", "portal", "staff", "dev", "test", "staging", "stage",
            "beta", "mail", "smtp", "cdn", "assets", "static", "status", "docs", "support", "help", "blog", "sales",
            "pricing", "demo", "auth", "login", "ai", "agent", "terminal", "renderer", "preview");

    private final Resolver resolver;

    public UrlPolicy() {
        this(InetAddress::getAllByName);
    }

    public UrlPolicy(Resolver resolver) {
        this.resolver = resolver;
    }

    /**
     * Normalises what the visitor typed and validates it.
     *
     * @throws InvalidUrlException with a sentence that can be shown to the visitor
     */
    public URI normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new InvalidUrlException("Enter the address of the hotel website, e.g. https://example.com");
        }
        String s = raw.trim();
        if (s.length() > MAX_LENGTH) {
            throw new InvalidUrlException("That address is too long to be a website address.");
        }
        if (!s.matches("(?i)^[a-z][a-z0-9+.-]*://.*")) {
            s = "https://" + s;
        }
        URI uri;
        try {
            uri = new URI(s);
        } catch (java.net.URISyntaxException e) {
            throw new InvalidUrlException("That does not look like a website address.");
        }
        validate(uri);
        AcquisitionUrlNormalizer.NormalizationResult normalized = AcquisitionUrlNormalizer.normalizeAndValidate(uri.toString(), false);
        if (!normalized.valid()) {
            throw new InvalidUrlException(messageFor(normalized));
        }
        return URI.create(normalized.normalizedUrl());
    }

    /** Validates an already-parsed URL, e.g. a link discovered on the site before it is fetched. */
    public void validate(URI uri) {
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        if (!scheme.equals("http") && !scheme.equals("https")) {
            throw new InvalidUrlException("Only http:// and https:// addresses can be checked.");
        }
        if (uri.getUserInfo() != null) {
            throw new InvalidUrlException("Addresses with a user name or password cannot be checked.");
        }
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new InvalidUrlException("That does not look like a website address.");
        }
        host = host.toLowerCase(Locale.ROOT);
        if (host.startsWith("[") && host.endsWith("]")) {
            host = host.substring(1, host.length() - 1);
        }
        if (host.equals("localhost") || host.endsWith(".localhost") || host.endsWith(".local")
                || host.endsWith(".internal") || host.endsWith(".home.arpa")) {
            throw new InvalidUrlException("Local or internal addresses cannot be checked.");
        }
        rejectOwnHosts(host);
        InetAddress[] addresses;
        try {
            addresses = resolver.resolve(host);
        } catch (UnknownHostException e) {
            throw new InvalidUrlException("We could not find a website at " + host + ".");
        }
        if (addresses == null || addresses.length == 0) {
            throw new InvalidUrlException("We could not find a website at " + host + ".");
        }
        for (InetAddress address : addresses) {
            if (!AcquisitionUrlNormalizer.isPublicIpAddress(address, false) || isUniqueLocal(address)) {
                throw new InvalidUrlException("Local or internal addresses cannot be checked.");
            }
        }
        if (uri.getPort() != -1 && uri.getPort() != 80 && uri.getPort() != 443) {
            throw new InvalidUrlException("Only the standard web ports can be checked.");
        }
    }

    /** fc00::/7, the IPv6 private range, which the acquisition normaliser does not cover. */
    private static boolean isUniqueLocal(InetAddress address) {
        return address instanceof Inet6Address && (address.getAddress()[0] & 0xfe) == 0xfc;
    }

    private static void rejectOwnHosts(String host) {
        for (String own : OWN_DOMAINS) {
            if (host.equals(own) || host.equals("www." + own)) {
                throw new InvalidUrlException("That is Antena's own site; enter a hotel's website instead.");
            }
            if (host.endsWith("." + own)) {
                String sub = host.substring(0, host.length() - own.length() - 1);
                if (sub.contains(".") || RESERVED_SUBDOMAINS.contains(sub)) {
                    throw new InvalidUrlException("That is Antena's own site; enter a hotel's website instead.");
                }
            }
        }
    }

    private static String messageFor(AcquisitionUrlNormalizer.NormalizationResult result) {
        return switch (result.outcome()) {
            case UNSUPPORTED_SCHEME -> "Only http:// and https:// addresses can be checked.";
            case INVALID_TARGET -> result.errorMessage() != null && result.errorMessage().contains("non-public")
                    ? "Local or internal addresses cannot be checked." : "That does not look like a website address.";
            default -> "That address cannot be checked.";
        };
    }
}
