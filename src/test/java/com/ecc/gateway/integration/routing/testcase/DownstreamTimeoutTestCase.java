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
    HttpStatus expectedStatus
) {
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
        return DownstreamTimeoutTestCase.builder()
            .path("/api/jobs")
            .requestHeaders(validTokenHeaders())
            .expectedStatus(HttpStatus.GATEWAY_TIMEOUT)
            .build();
    }

    private static DownstreamTimeoutTestCase companyServiceTooSlowTestCase() {
        return DownstreamTimeoutTestCase.builder()
            .path("/api/companies")
            .requestHeaders(validTokenHeaders())
            .expectedStatus(HttpStatus.GATEWAY_TIMEOUT)
            .build();
    }
}
