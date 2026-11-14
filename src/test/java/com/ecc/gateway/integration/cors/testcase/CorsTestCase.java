package com.ecc.gateway.integration.cors.testcase;

import static com.ecc.gateway.integration.support.GatewayTestProperties.ALLOWED_ORIGIN_V;
import static com.ecc.gateway.integration.support.TestHeaders.validTokenHeaders;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

import java.util.stream.Stream;
import lombok.Builder;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.support.ParameterDeclarations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

@Builder
public record CorsTestCase(
    // input
    HttpMethod method,
    String path,
    HttpHeaders requestHeaders,
    // expected results
    HttpStatus expectedStatus,
    String expectedAllowOrigin,
    String expectedAllowCredentials,
    long expectedDownstreamRequests
) {
    private static final String FOREIGN_ORIGIN_V = "https://evil.test";

    public static class ArgumentsProvider implements org.junit.jupiter.params.provider.ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
            return Stream.of(
                // negative test cases
                argumentSet("Preflight from foreign origin", preflightFromForeignOriginTestCase()),
                argumentSet("Preflight with not allowed method", preflightWithNotAllowedMethodTestCase()),
                argumentSet("Actual GET from foreign origin with token", actualGetFromForeignOriginTestCase()),

                // positive test cases
                argumentSet("Preflight GET from UI origin without token", preflightGetTestCase()),
                argumentSet("Preflight DELETE from UI origin without token", preflightDeleteTestCase()),
                argumentSet("Actual GET from UI origin with token", actualGetTestCase())
            );
        }
    }

    private static CorsTestCase preflightFromForeignOriginTestCase() {
        return CorsTestCase.builder()
            .method(HttpMethod.OPTIONS)
            .path("/api/jobs")
            .requestHeaders(preflightHeaders(FOREIGN_ORIGIN_V, HttpMethod.GET))
            .expectedStatus(HttpStatus.FORBIDDEN)
            .build();
    }

    private static CorsTestCase preflightWithNotAllowedMethodTestCase() {
        return CorsTestCase.builder()
            .method(HttpMethod.OPTIONS)
            .path("/api/jobs")
            .requestHeaders(preflightHeaders(ALLOWED_ORIGIN_V, HttpMethod.TRACE))
            .expectedStatus(HttpStatus.FORBIDDEN)
            .build();
    }

    private static CorsTestCase actualGetFromForeignOriginTestCase() {
        HttpHeaders headers = validTokenHeaders();
        headers.setOrigin(FOREIGN_ORIGIN_V);

        return CorsTestCase.builder().method(HttpMethod.GET).path("/api/jobs").requestHeaders(headers).expectedStatus(HttpStatus.FORBIDDEN).build();
    }

    private static CorsTestCase preflightGetTestCase() {
        return CorsTestCase.builder()
            .method(HttpMethod.OPTIONS)
            .path("/api/jobs")
            .requestHeaders(preflightHeaders(ALLOWED_ORIGIN_V, HttpMethod.GET))
            .expectedStatus(HttpStatus.OK)
            .expectedAllowOrigin(ALLOWED_ORIGIN_V)
            .build();
    }

    private static CorsTestCase preflightDeleteTestCase() {
        return CorsTestCase.builder()
            .method(HttpMethod.OPTIONS)
            .path("/api/jobs/42")
            .requestHeaders(preflightHeaders(ALLOWED_ORIGIN_V, HttpMethod.DELETE))
            .expectedStatus(HttpStatus.OK)
            .expectedAllowOrigin(ALLOWED_ORIGIN_V)
            .build();
    }

    private static CorsTestCase actualGetTestCase() {
        HttpHeaders headers = validTokenHeaders();
        headers.setOrigin(ALLOWED_ORIGIN_V);

        return CorsTestCase.builder()
            .method(HttpMethod.GET)
            .path("/api/jobs")
            .requestHeaders(headers)
            .expectedStatus(HttpStatus.OK)
            .expectedAllowOrigin(ALLOWED_ORIGIN_V)
            .expectedDownstreamRequests(1)
            .build();
    }

    private static HttpHeaders preflightHeaders(String origin, HttpMethod requestMethod) {
        HttpHeaders headers = new HttpHeaders();
        headers.setOrigin(origin);
        headers.setAccessControlRequestMethod(requestMethod);
        headers.set(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, HttpHeaders.AUTHORIZATION);
        return headers;
    }
}
