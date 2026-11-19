package com.ecc.gateway.integration.root.testcase;

import static com.ecc.gateway.integration.support.TestHeaders.LOCAL_HOST_V;
import static com.ecc.gateway.integration.support.TestHeaders.noHeaders;
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
public record RootLinksTestCase(
    // input
    String path,
    HttpHeaders requestHeaders,
    // expected results
    HttpStatus expectedStatus,
    String expectedBody
) {
    private static final String ROOT_PATH_V = "/api";

    private static final String PUBLIC_HTTPS_BASE_URL_V = "https://ecc-api.dev.local";
    private static final String LOCAL_HTTP_BASE_URL_V = "http://ecc-api.local";

    private static final String UNAUTHORIZED_BODY_V =
        "{\"status\":\"BAD_CREDENTIALS\",\"data\":null,\"errors\":[{\"code\":\"BAD_CREDENTIALS\",\"message\":\"Missing or invalid access token\"}]}";

    public static class ArgumentsProvider implements org.junit.jupiter.params.provider.ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
            return Stream.of(
                // negative test cases
                argumentSet("Request without token", requestWithoutTokenTestCase()),

                // positive test cases
                argumentSet("HTTPS request from Traefik", traefikHttpsTestCase()),
                argumentSet("Plain HTTP request without forwarded headers", plainHttpTestCase())
            );
        }
    }

    private static RootLinksTestCase requestWithoutTokenTestCase() {
        return RootLinksTestCase.builder()
            .path(ROOT_PATH_V)
            .requestHeaders(noHeaders())
            .expectedStatus(HttpStatus.UNAUTHORIZED)
            .expectedBody(UNAUTHORIZED_BODY_V)
            .build();
    }

    private static RootLinksTestCase traefikHttpsTestCase() {
        return RootLinksTestCase.builder()
            .path(ROOT_PATH_V)
            .requestHeaders(traefikHttpsHeaders())
            .expectedStatus(HttpStatus.OK)
            .expectedBody(rootLinksJson(PUBLIC_HTTPS_BASE_URL_V))
            .build();
    }

    private static RootLinksTestCase plainHttpTestCase() {
        HttpHeaders headers = validTokenHeaders();
        headers.set(HttpHeaders.HOST, LOCAL_HOST_V);

        return RootLinksTestCase.builder()
            .path(ROOT_PATH_V)
            .requestHeaders(headers)
            .expectedStatus(HttpStatus.OK)
            .expectedBody(rootLinksJson(LOCAL_HTTP_BASE_URL_V))
            .build();
    }

    private static String rootLinksJson(String baseUrl) {
        String jobsJson = "\"jobs\":{\"href\":\"" + baseUrl + "/api/jobs\"}";
        String jobJson = "\"job\":{\"href\":\"" + baseUrl + "/api/jobs/{jobId}\",\"templated\":true}";
        String companiesJson = "\"companies\":{\"href\":\"" + baseUrl + "/api/companies\"}";
        String marketsJson = "\"markets\":{\"href\":\"" + baseUrl + "/api/markets\"}";

        return "{\"status\":\"OK\",\"data\":{\"_links\":{" + jobsJson + "," + jobJson + "," + companiesJson + "," + marketsJson + "}},\"errors\":[]}";
    }
}
