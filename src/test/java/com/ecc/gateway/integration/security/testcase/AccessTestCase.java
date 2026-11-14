package com.ecc.gateway.integration.security.testcase;

import static com.ecc.gateway.integration.support.TestHeaders.bearerHeaders;
import static com.ecc.gateway.integration.support.TestHeaders.noHeaders;
import static com.ecc.gateway.integration.support.TestHeaders.validTokenHeaders;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

import com.ecc.gateway.integration.support.jwt.TestJwtIssuer;
import java.util.stream.Stream;
import lombok.Builder;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.support.ParameterDeclarations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

@Builder
public record AccessTestCase(
    // input
    String path,
    HttpHeaders requestHeaders,
    // expected results
    HttpStatus expectedStatus,
    boolean expectedBearerChallenge,
    long expectedDownstreamRequests
) {
    public static class ArgumentsProvider implements org.junit.jupiter.params.provider.ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
            return Stream.of(
                // negative test cases
                argumentSet("Api root without token", apiRootWithoutTokenTestCase()),
                argumentSet("Jobs without token", jobsWithoutTokenTestCase()),
                argumentSet("Companies without token", companiesWithoutTokenTestCase()),
                argumentSet("Not public actuator endpoint without token", notPublicActuatorEndpointTestCase()),
                argumentSet("Path resembling health endpoint without token", pathResemblingHealthTestCase()),
                argumentSet("Unknown path without token", unknownPathTestCase()),
                argumentSet("Expired token", expiredTokenTestCase()),
                argumentSet("Token from foreign issuer", foreignIssuerTokenTestCase()),
                argumentSet("Token signed with foreign key", foreignKeyTokenTestCase()),
                argumentSet("Malformed token", malformedTokenTestCase()),
                argumentSet("Token without Bearer prefix", tokenWithoutBearerPrefixTestCase()),

                // positive test cases
                argumentSet("Health endpoint without token", healthWithoutTokenTestCase()),
                argumentSet("Gateway api docs without token", gatewayApiDocsWithoutTokenTestCase()),
                argumentSet("Swagger UI without token", swaggerUiWithoutTokenTestCase()),
                argumentSet("Job service api docs without token", jobServiceApiDocsWithoutTokenTestCase()),
                argumentSet("Company service api docs without token", companyServiceApiDocsWithoutTokenTestCase()),
                argumentSet("Api root with valid token", apiRootWithValidTokenTestCase()),
                argumentSet("Jobs with valid token", jobsWithValidTokenTestCase()),
                argumentSet("Markets with valid token", marketsWithValidTokenTestCase())
            );
        }
    }

    private static AccessTestCase apiRootWithoutTokenTestCase() {
        return unauthorized("/api", noHeaders());
    }

    private static AccessTestCase jobsWithoutTokenTestCase() {
        return unauthorized("/api/jobs", noHeaders());
    }

    private static AccessTestCase companiesWithoutTokenTestCase() {
        return unauthorized("/api/companies", noHeaders());
    }

    private static AccessTestCase notPublicActuatorEndpointTestCase() {
        return unauthorized("/actuator/env", noHeaders());
    }

    private static AccessTestCase pathResemblingHealthTestCase() {
        return unauthorized("/actuator/healthz", noHeaders());
    }

    private static AccessTestCase unknownPathTestCase() {
        return unauthorized("/unknown", noHeaders());
    }

    private static AccessTestCase expiredTokenTestCase() {
        return unauthorized("/api", bearerHeaders(TestJwtIssuer.expiredToken()));
    }

    private static AccessTestCase foreignIssuerTokenTestCase() {
        return unauthorized("/api", bearerHeaders(TestJwtIssuer.foreignIssuerToken()));
    }

    private static AccessTestCase foreignKeyTokenTestCase() {
        return unauthorized("/api", bearerHeaders(TestJwtIssuer.foreignKeyToken()));
    }

    private static AccessTestCase malformedTokenTestCase() {
        return unauthorized("/api", bearerHeaders("not-a-jwt"));
    }

    private static AccessTestCase tokenWithoutBearerPrefixTestCase() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, TestJwtIssuer.validToken());

        return unauthorized("/api", headers);
    }

    private static AccessTestCase healthWithoutTokenTestCase() {
        return AccessTestCase.builder().path("/actuator/health").requestHeaders(noHeaders()).expectedStatus(HttpStatus.OK).build();
    }

    private static AccessTestCase gatewayApiDocsWithoutTokenTestCase() {
        return AccessTestCase.builder().path("/v3/api-docs").requestHeaders(noHeaders()).expectedStatus(HttpStatus.OK).build();
    }

    private static AccessTestCase swaggerUiWithoutTokenTestCase() {
        return AccessTestCase.builder().path("/swagger-ui.html").requestHeaders(noHeaders()).expectedStatus(HttpStatus.FOUND).build();
    }

    private static AccessTestCase jobServiceApiDocsWithoutTokenTestCase() {
        return AccessTestCase.builder()
            .path("/jobs/v3/api-docs")
            .requestHeaders(noHeaders())
            .expectedStatus(HttpStatus.OK)
            .expectedDownstreamRequests(1)
            .build();
    }

    private static AccessTestCase companyServiceApiDocsWithoutTokenTestCase() {
        return AccessTestCase.builder()
            .path("/companies/v3/api-docs")
            .requestHeaders(noHeaders())
            .expectedStatus(HttpStatus.OK)
            .expectedDownstreamRequests(1)
            .build();
    }

    private static AccessTestCase apiRootWithValidTokenTestCase() {
        return AccessTestCase.builder().path("/api").requestHeaders(validTokenHeaders()).expectedStatus(HttpStatus.OK).build();
    }

    private static AccessTestCase jobsWithValidTokenTestCase() {
        return AccessTestCase.builder()
            .path("/api/jobs")
            .requestHeaders(validTokenHeaders())
            .expectedStatus(HttpStatus.OK)
            .expectedDownstreamRequests(1)
            .build();
    }

    private static AccessTestCase marketsWithValidTokenTestCase() {
        return AccessTestCase.builder()
            .path("/api/markets")
            .requestHeaders(validTokenHeaders())
            .expectedStatus(HttpStatus.OK)
            .expectedDownstreamRequests(1)
            .build();
    }

    private static AccessTestCase unauthorized(String path, HttpHeaders requestHeaders) {
        return AccessTestCase.builder()
            .path(path)
            .requestHeaders(requestHeaders)
            .expectedStatus(HttpStatus.UNAUTHORIZED)
            .expectedBearerChallenge(true)
            .expectedDownstreamRequests(0)
            .build();
    }
}
