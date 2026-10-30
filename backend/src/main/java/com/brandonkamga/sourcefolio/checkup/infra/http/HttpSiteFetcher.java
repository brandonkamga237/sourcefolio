package com.brandonkamga.sourcefolio.checkup.infra.http;

import com.brandonkamga.sourcefolio.checkup.domain.port.SiteFetcher;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import java.util.OptionalInt;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fetches a URL a member gave us, so it must never become a way to reach our own network
 * (SSRF): every hop, redirects included, is resolved first and refused if any of its addresses
 * is private, loopback, link-local or otherwise not on the public internet. Redirects are
 * followed by hand for that reason.
 *
 * The HTTP client resolves the name again when it connects, so a DNS answer that changes in
 * between (rebinding) is not covered; only the status is read and the body is discarded, which
 * keeps what such a request could reveal to nothing.
 */
public class HttpSiteFetcher implements SiteFetcher {

    private static final Logger log = LoggerFactory.getLogger(HttpSiteFetcher.class);
    private static final int MAX_REDIRECTS = 5;
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final HttpClient http;
    private final String userAgent;
    private final Predicate<InetAddress> allowed;

    public HttpSiteFetcher(String userAgent) {
        this(userAgent, HttpSiteFetcher::isPublic);
    }

    HttpSiteFetcher(String userAgent, Predicate<InetAddress> allowed) {
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
        this.userAgent = userAgent;
        this.allowed = allowed;
    }

    @Override
    public OptionalInt statusOf(String url) {
        try {
            URI target = URI.create(url);
            for (int hop = 0; hop <= MAX_REDIRECTS; hop++) {
                if (!isAllowed(target)) {
                    log.info("Refused to fetch {}: not a public http(s) address", target);
                    return OptionalInt.empty();
                }
                HttpResponse<Void> response = http.send(HttpRequest.newBuilder(target)
                        .timeout(TIMEOUT)
                        .header("User-Agent", userAgent)
                        .GET()
                        .build(), HttpResponse.BodyHandlers.discarding());
                int status = response.statusCode();
                var location = response.headers().firstValue("Location");
                if (status < 300 || status >= 400 || location.isEmpty()) {
                    return OptionalInt.of(status);
                }
                target = target.resolve(location.get());
            }
            log.info("Gave up on {} after {} redirects", url, MAX_REDIRECTS);
            return OptionalInt.empty();
        } catch (IOException | IllegalArgumentException unreachable) {
            log.info("Could not fetch {}: {}", url, unreachable.toString());
            return OptionalInt.empty();
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            return OptionalInt.empty();
        }
    }

    private boolean isAllowed(URI target) {
        String scheme = target.getScheme() == null ? "" : target.getScheme().toLowerCase(Locale.ROOT);
        if (!scheme.equals("http") && !scheme.equals("https") || target.getHost() == null || target.getRawUserInfo() != null) {
            return false;
        }
        try {
            InetAddress[] addresses = InetAddress.getAllByName(target.getHost());
            for (InetAddress address : addresses) {
                if (!allowed.test(address)) {
                    return false;
                }
            }
            return addresses.length > 0;
        } catch (UnknownHostException unknown) {
            return false;
        }
    }

    static boolean isPublic(InetAddress address) {
        if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()
                || address.isSiteLocalAddress() || address.isMulticastAddress()) {
            return false;
        }
        byte[] bytes = address.getAddress();
        if (address instanceof Inet4Address) {
            int first = bytes[0] & 0xff;
            int second = bytes[1] & 0xff;
            return first != 0                                   // "this network"
                    && !(first == 100 && second >= 64 && second < 128) // carrier-grade NAT
                    && !(first == 192 && second == 0 && (bytes[2] & 0xff) == 0)
                    && !(first == 198 && (second == 18 || second == 19)) // benchmarking
                    && first < 240;                              // reserved and broadcast
        }
        if (address instanceof Inet6Address) {
            return (bytes[0] & 0xfe) != 0xfc; // unique local fc00::/7
        }
        return false;
    }
}
