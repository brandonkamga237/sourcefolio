package com.brandonkamga.sourcefolio.checkup.infra.http;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import static org.assertj.core.api.Assertions.assertThat;

class HttpSiteFetcherTest {

    private HttpServer server;
    private String origin;
    private final List<String> userAgents = new ArrayList<>();

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/ok", exchange -> {
            userAgents.add(exchange.getRequestHeaders().getFirst("User-Agent"));
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        server.createContext("/missing", exchange -> {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
        });
        server.createContext("/moved", exchange -> {
            exchange.getResponseHeaders().add("Location", "/ok");
            exchange.sendResponseHeaders(301, -1);
            exchange.close();
        });
        server.createContext("/loop", exchange -> {
            exchange.getResponseHeaders().add("Location", "/loop");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        });
        server.start();
        origin = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    // The test server is on loopback, which the real guard refuses: allow it here only.
    private HttpSiteFetcher fetcherAllowingLoopback() {
        return new HttpSiteFetcher("SourceFolio (+https://sourcefolio.test)", address -> true);
    }

    @Test
    @DisplayName("returns the status of the page and identifies itself")
    void status() {
        assertThat(fetcherAllowingLoopback().statusOf(origin + "/ok")).hasValue(200);
        assertThat(fetcherAllowingLoopback().statusOf(origin + "/missing")).hasValue(404);
        assertThat(userAgents).containsExactly("SourceFolio (+https://sourcefolio.test)");
    }

    @Test
    @DisplayName("follows redirects to the final page")
    void followsRedirects() {
        assertThat(fetcherAllowingLoopback().statusOf(origin + "/moved")).hasValue(200);
    }

    @Test
    @DisplayName("gives up on a redirect loop")
    void redirectLoop() {
        assertThat(fetcherAllowingLoopback().statusOf(origin + "/loop")).isEmpty();
    }

    @Test
    @DisplayName("never fetches our own machine or network: loopback is refused before any request")
    void refusesLoopback() {
        assertThat(new HttpSiteFetcher("SourceFolio").statusOf(origin + "/ok")).isEmpty();
        assertThat(userAgents).isEmpty();
    }

    @Test
    @DisplayName("a redirect towards a private address is refused too")
    void refusesRedirectToPrivateAddress() {
        server.createContext("/to-metadata", exchange -> {
            exchange.getResponseHeaders().add("Location", "http://169.254.169.254/latest/meta-data/");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        });
        HttpSiteFetcher fetcher = new HttpSiteFetcher("SourceFolio",
                address -> address.isLoopbackAddress() || HttpSiteFetcher.isPublic(address));

        assertThat(fetcher.statusOf(origin + "/to-metadata")).isEmpty();
    }

    @Test
    @DisplayName("only http and https are fetched")
    void refusesOtherSchemes() {
        assertThat(fetcherAllowingLoopback().statusOf("file:///etc/passwd")).isEqualTo(OptionalInt.empty());
        assertThat(fetcherAllowingLoopback().statusOf("ftp://example.org/")).isEmpty();
    }

    @Test
    @DisplayName("private, shared and reserved ranges are not public; ordinary addresses are")
    void publicAddresses() throws IOException {
        for (String internal : List.of("127.0.0.1", "10.1.2.3", "172.16.0.1", "192.168.1.1", "169.254.169.254",
                "100.64.0.1", "0.0.0.0", "198.18.0.1", "240.0.0.1", "::1", "fd00::1", "fe80::1")) {
            assertThat(HttpSiteFetcher.isPublic(InetAddress.getByName(internal))).as(internal).isFalse();
        }
        for (String external : List.of("185.199.108.153", "8.8.8.8", "2606:4700::1111")) {
            assertThat(HttpSiteFetcher.isPublic(InetAddress.getByName(external))).as(external).isTrue();
        }
    }
}
