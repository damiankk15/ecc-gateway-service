package com.ecc.gateway.integration.routing.testcase;

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
public record DownstreamUnavailableTestCase(
    // input
    String path,
    HttpHeaders requestHeaders,
    // expected results
    HttpStatus expectedStatus,
    String expectedBody
) {
    private static final String JOBS_PATH_V = "/api/jobs";
    private static final String COMPANIES_PATH_V = "/api/companies";

    private static final String SERVICE_UNAVAILABLE_BODY_V =
        "{\"status\":\"SERVICE_UNAVAILABLE\",\"data\":null,\"errors\":[{\"code\":\"SERVICE_UNAVAILABLE\",\"message\":\"Service is temporarily unavailable\"}]}";

    public static class ArgumentsProvider implements org.junit.jupiter.params.provider.ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
            return Stream.of(
                argumentSet("Job service without running instances refuses connection", jobServiceDownTestCase()),
                argumentSet("Company service removed so its host does not resolve", companyServiceDownTestCase())
            );
        }
    }

    private static DownstreamUnavailableTestCase jobServiceDownTestCase() {
        return DownstreamUnavailableTestCase.builder()
            .path(JOBS_PATH_V)
            .requestHeaders(validTokenHeaders())
            .expectedStatus(HttpStatus.SERVICE_UNAVAILABLE)
            .expectedBody(SERVICE_UNAVAILABLE_BODY_V)
            .build();
    }

    private static DownstreamUnavailableTestCase companyServiceDownTestCase() {
        return DownstreamUnavailableTestCase.builder()
            .path(COMPANIES_PATH_V)
            .requestHeaders(validTokenHeaders())
            .expectedStatus(HttpStatus.SERVICE_UNAVAILABLE)
            .expectedBody(SERVICE_UNAVAILABLE_BODY_V)
            .build();
    }
}
