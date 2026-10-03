package com.antenapro.hotelcheck.acquisition;

import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.SystemDefaultDnsResolver;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class PublicWebDnsResolver implements DnsResolver {

    private final boolean allowLocalhost;
    private final DnsResolver delegate;

    public PublicWebDnsResolver(boolean allowLocalhost) {
        this.allowLocalhost = allowLocalhost;
        this.delegate = SystemDefaultDnsResolver.INSTANCE;
    }

    @Override
    public InetAddress[] resolve(String host) throws UnknownHostException {
        InetAddress[] addresses = delegate.resolve(host);
        if (addresses == null || addresses.length == 0) {
            throw new UnknownHostException("Host '" + host + "' resolved to 0 IP addresses");
        }

        for (InetAddress addr : addresses) {
            if (!AcquisitionUrlNormalizer.isPublicIpAddress(addr, allowLocalhost)) {
                throw new UnknownHostException("Target host '" + host + "' resolves to non-public network address: " + addr.getHostAddress());
            }
        }
        return addresses;
    }

    @Override
    public String resolveCanonicalHostname(String host) throws UnknownHostException {
        return delegate.resolveCanonicalHostname(host);
    }
}
