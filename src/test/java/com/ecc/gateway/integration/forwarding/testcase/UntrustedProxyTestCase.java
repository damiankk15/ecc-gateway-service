package com.ecc.gateway.integration.forwarding.testcase;

import static com.ecc.gateway.integration.support.TestHeaders.traefikHttpsHeaders;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

import java.util.stream.Stream;
import lombok.Builder;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.support.ParameterDeclarations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

@Builder
public record UntrustedProxyTestCase(
    // input
    String path,
    HttpHeaders requestHeaders,
    // expected results
    HttpStatus expectedStatus,
    String expectedForwardedProto
) {
    public static class ArgumentsProvider implements org.junit.jupiter.params.provider.ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
            return Stream.of(argumentSet("Spoofed HTTPS headers from untrusted address", spoofedHttpsHeadersTestCase()));
        }
    }

    private static UntrustedProxyTestCase spoofedHttpsHeadersTestCase() {
        return UntrustedProxyTestCase.builder()
            .path("/api/jobs")
            .requestHeaders(traefikHttpsHeaders())
            .expectedStatus(HttpStatus.OK)
            .expectedForwardedProto("http")
            .build();
    }
}
