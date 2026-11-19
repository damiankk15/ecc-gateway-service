package com.ecc.gateway.integration.forwarding.testcase;

import static com.ecc.gateway.integration.support.TestHeaders.LOCAL_HOST_V;
import static com.ecc.gateway.integration.support.TestHeaders.PUBLIC_HOST_V;
import static com.ecc.gateway.integration.support.TestHeaders.traefikHttpsHeaders;
import static com.ecc.gateway.integration.support.TestHeaders.validTokenHeaders;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

import java.util.stream.Stream;
import lombok.Builder;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.support.ParameterDeclarations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

@Builder
public record ForwardedHeadersTestCase(
    // input
    String path,
    HttpHeaders requestHeaders,
    // expected results
    HttpStatus expectedStatus,
    String expectedForwardedProto,
    String expectedForwardedHost
) {
    private static final String JOBS_PATH_V = "/api/jobs";
    private static final String COMPANIES_PATH_V = "/api/companies";

    private static final String HTTPS_V = "https";
    private static final String HTTP_V = "http";

    public static class ArgumentsProvider implements org.junit.jupiter.params.provider.ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
            return Stream.of(
                argumentSet("HTTPS request from Traefik to job service", traefikHttpsToJobServiceTestCase()),
                argumentSet("HTTPS request from Traefik to company service", traefikHttpsToCompanyServiceTestCase()),
                argumentSet("Plain HTTP request without forwarded headers", plainHttpTestCase())
            );
        }
    }

    private static ForwardedHeadersTestCase traefikHttpsToJobServiceTestCase() {
        return ForwardedHeadersTestCase.builder()
            .path(JOBS_PATH_V)
            .requestHeaders(traefikHttpsHeaders())
            .expectedStatus(HttpStatus.OK)
            .expectedForwardedProto(HTTPS_V)
            .expectedForwardedHost(PUBLIC_HOST_V)
            .build();
    }

    private static ForwardedHeadersTestCase traefikHttpsToCompanyServiceTestCase() {
        return ForwardedHeadersTestCase.builder()
            .path(COMPANIES_PATH_V)
            .requestHeaders(traefikHttpsHeaders())
            .expectedStatus(HttpStatus.OK)
            .expectedForwardedProto(HTTPS_V)
            .expectedForwardedHost(PUBLIC_HOST_V)
            .build();
    }

    private static ForwardedHeadersTestCase plainHttpTestCase() {
        HttpHeaders headers = validTokenHeaders();
        headers.set(HttpHeaders.HOST, LOCAL_HOST_V);

        return ForwardedHeadersTestCase.builder()
            .path(JOBS_PATH_V)
            .requestHeaders(headers)
            .expectedStatus(HttpStatus.OK)
            .expectedForwardedProto(HTTP_V)
            .expectedForwardedHost(LOCAL_HOST_V)
            .build();
    }
}
