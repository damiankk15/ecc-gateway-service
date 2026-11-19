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
public record DownstreamTimeoutTestCase(
    // input
    String path,
    HttpHeaders requestHeaders,
    // expected results
    HttpStatus expectedStatus,
    String expectedBody
) {
    private static final String JOBS_PATH_V = "/api/jobs";
    private static final String COMPANIES_PATH_V = "/api/companies";

    private static final String GATEWAY_TIMEOUT_BODY_V =
        "{\"status\":\"SERVICE_UNAVAILABLE\",\"data\":null,\"errors\":[{\"code\":\"GATEWAY_TIMEOUT\",\"message\":\"Service did not respond in time\"}]}";

    public static class ArgumentsProvider implements org.junit.jupiter.params.provider.ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
            return Stream.of(
                argumentSet("Job service responds too slowly", jobServiceTooSlowTestCase()),
                argumentSet("Company service responds too slowly", companyServiceTooSlowTestCase())
            );
        }
    }

    private static DownstreamTimeoutTestCase jobServiceTooSlowTestCase() {
        return gatewayTimeout(JOBS_PATH_V);
    }

    private static DownstreamTimeoutTestCase companyServiceTooSlowTestCase() {
        return gatewayTimeout(COMPANIES_PATH_V);
    }

    private static DownstreamTimeoutTestCase gatewayTimeout(String path) {
        return DownstreamTimeoutTestCase.builder()
            .path(path)
            .requestHeaders(validTokenHeaders())
            .expectedStatus(HttpStatus.GATEWAY_TIMEOUT)
            .expectedBody(GATEWAY_TIMEOUT_BODY_V)
            .build();
    }
}
