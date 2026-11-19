package com.ecc.gateway.web.testcase;

import static org.junit.jupiter.params.provider.Arguments.argumentSet;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;
import java.util.stream.Stream;
import lombok.Builder;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.support.ParameterDeclarations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;

@Builder
public record ApplyTestCase(
    // input
    ServerHttpRequest request,
    // mocked values
    String mockedTrustedProxies,
    // expected results
    URI expectedUri,
    List<String> expectedHeaderNames
) {
    private static final String X_FORWARDED_PROTO_F = "X-Forwarded-Proto";
    private static final String X_FORWARDED_HOST_F = "X-Forwarded-Host";
    private static final String FORWARDED_F = "Forwarded";

    private static final String TRUSTED_NETWORK_V = "10\\..*";
    private static final String SINGLE_TRUSTED_PROXY_V = "10\\.0\\.0\\.1";

    private static final String LOCAL_URL_V = "http://ecc-api.local/api";
    private static final String PUBLIC_URL_V = "https://ecc-api.dev.local/api";
    private static final String PUBLIC_HOST_V = "ecc-api.dev.local";

    private static final InetSocketAddress TRUSTED_ADDRESS_V = new InetSocketAddress("10.0.0.1", 40000);
    private static final InetSocketAddress UNTRUSTED_ADDRESS_V = new InetSocketAddress("127.0.0.1", 40000);
    private static final InetSocketAddress PARTIALLY_MATCHING_ADDRESS_V = new InetSocketAddress("10.0.0.10", 40000);

    public static class ArgumentsProvider implements org.junit.jupiter.params.provider.ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
            return Stream.of(
                // negative test cases
                argumentSet("Spoofed X-Forwarded headers from untrusted address", untrustedXForwardedHeadersTestCase()),
                argumentSet("Spoofed Forwarded header from untrusted address", untrustedForwardedHeaderTestCase()),
                argumentSet("Spoofed headers without remote address", noRemoteAddressTestCase()),
                argumentSet("Address only partially matching trusted proxies", partiallyMatchingAddressTestCase()),

                // positive test cases
                argumentSet("X-Forwarded headers from trusted proxy", trustedXForwardedHeadersTestCase()),
                argumentSet("Forwarded header from trusted proxy", trustedForwardedHeaderTestCase()),
                argumentSet("Trusted proxy without forwarded headers", trustedWithoutForwardedHeadersTestCase())
            );
        }
    }

    private static ApplyTestCase untrustedXForwardedHeadersTestCase() {
        return ApplyTestCase.builder()
            .request(xForwardedRequest(UNTRUSTED_ADDRESS_V))
            .mockedTrustedProxies(TRUSTED_NETWORK_V)
            .expectedUri(URI.create(LOCAL_URL_V))
            .expectedHeaderNames(List.of(HttpHeaders.ACCEPT))
            .build();
    }

    private static ApplyTestCase untrustedForwardedHeaderTestCase() {
        return ApplyTestCase.builder()
            .request(forwardedRequest(UNTRUSTED_ADDRESS_V))
            .mockedTrustedProxies(TRUSTED_NETWORK_V)
            .expectedUri(URI.create(LOCAL_URL_V))
            .expectedHeaderNames(List.of(HttpHeaders.ACCEPT))
            .build();
    }

    private static ApplyTestCase noRemoteAddressTestCase() {
        return ApplyTestCase.builder()
            .request(xForwardedRequest(null))
            .mockedTrustedProxies(TRUSTED_NETWORK_V)
            .expectedUri(URI.create(LOCAL_URL_V))
            .expectedHeaderNames(List.of(HttpHeaders.ACCEPT))
            .build();
    }

    private static ApplyTestCase partiallyMatchingAddressTestCase() {
        return ApplyTestCase.builder()
            .request(xForwardedRequest(PARTIALLY_MATCHING_ADDRESS_V))
            .mockedTrustedProxies(SINGLE_TRUSTED_PROXY_V)
            .expectedUri(URI.create(LOCAL_URL_V))
            .expectedHeaderNames(List.of(HttpHeaders.ACCEPT))
            .build();
    }

    private static ApplyTestCase trustedXForwardedHeadersTestCase() {
        return ApplyTestCase.builder()
            .request(xForwardedRequest(TRUSTED_ADDRESS_V))
            .mockedTrustedProxies(TRUSTED_NETWORK_V)
            .expectedUri(URI.create(PUBLIC_URL_V))
            .expectedHeaderNames(List.of(HttpHeaders.ACCEPT))
            .build();
    }

    private static ApplyTestCase trustedForwardedHeaderTestCase() {
        return ApplyTestCase.builder()
            .request(forwardedRequest(TRUSTED_ADDRESS_V))
            .mockedTrustedProxies(TRUSTED_NETWORK_V)
            .expectedUri(URI.create(PUBLIC_URL_V))
            .expectedHeaderNames(List.of(HttpHeaders.ACCEPT))
            .build();
    }

    private static ApplyTestCase trustedWithoutForwardedHeadersTestCase() {
        return ApplyTestCase.builder()
            .request(MockServerHttpRequest.get(LOCAL_URL_V).remoteAddress(TRUSTED_ADDRESS_V).header(HttpHeaders.ACCEPT, "application/json").build())
            .mockedTrustedProxies(TRUSTED_NETWORK_V)
            .expectedUri(URI.create(LOCAL_URL_V))
            .expectedHeaderNames(List.of(HttpHeaders.ACCEPT))
            .build();
    }

    private static ServerHttpRequest xForwardedRequest(InetSocketAddress remoteAddress) {
        return MockServerHttpRequest.get(LOCAL_URL_V)
            .remoteAddress(remoteAddress)
            .header(HttpHeaders.ACCEPT, "application/json")
            .header(X_FORWARDED_PROTO_F, "https")
            .header(X_FORWARDED_HOST_F, PUBLIC_HOST_V)
            .build();
    }

    private static ServerHttpRequest forwardedRequest(InetSocketAddress remoteAddress) {
        return MockServerHttpRequest.get(LOCAL_URL_V)
            .remoteAddress(remoteAddress)
            .header(HttpHeaders.ACCEPT, "application/json")
            .header(FORWARDED_F, "proto=https;host=" + PUBLIC_HOST_V)
            .build();
    }
}
