package com.ecc.gateway.integration.forwarding.testcase;

import static com.ecc.gateway.integration.support.TestHeaders.LOCAL_HOST_V;
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
public record UntrustedProxyRootLinksTestCase(
    // input
    String path,
    HttpHeaders requestHeaders,
    // expected results
    HttpStatus expectedStatus,
    String expectedBody
) {
    private static final String ROOT_PATH_V = "/api";

    private static final String LOCAL_HTTP_BASE_URL_V = "http://ecc-api.local";

    public static class ArgumentsProvider implements org.junit.jupiter.params.provider.ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
            return Stream.of(argumentSet("Spoofed HTTPS headers from untrusted address", spoofedHttpsHeadersTestCase()));
        }
    }

    private static UntrustedProxyRootLinksTestCase spoofedHttpsHeadersTestCase() {
        HttpHeaders headers = traefikHttpsHeaders();
        headers.set(HttpHeaders.HOST, LOCAL_HOST_V);

        return UntrustedProxyRootLinksTestCase.builder()
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
