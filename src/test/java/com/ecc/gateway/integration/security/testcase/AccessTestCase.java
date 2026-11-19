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
    private static final String API_ROOT_PATH_V = "/api";
    private static final String JOBS_PATH_V = "/api/jobs";
    private static final String COMPANIES_PATH_V = "/api/companies";
    private static final String MARKETS_PATH_V = "/api/markets";
    private static final String ACTUATOR_ENV_PATH_V = "/actuator/env";
    private static final String HEALTHZ_PATH_V = "/actuator/healthz";
    private static final String UNKNOWN_PATH_V = "/unknown";
    private static final String HEALTH_PATH_V = "/actuator/health";
    private static final String GATEWAY_DOCS_PATH_V = "/v3/api-docs";
    private static final String SWAGGER_UI_PATH_V = "/swagger-ui.html";
    private static final String JOB_SERVICE_DOCS_PATH_V = "/jobs/v3/api-docs";
    private static final String COMPANY_SERVICE_DOCS_PATH_V = "/companies/v3/api-docs";

    private static final String MALFORMED_TOKEN_V = "not-a-jwt";

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
        return unauthorized(API_ROOT_PATH_V, noHeaders());
    }

    private static AccessTestCase jobsWithoutTokenTestCase() {
        return unauthorized(JOBS_PATH_V, noHeaders());
    }

    private static AccessTestCase companiesWithoutTokenTestCase() {
        return unauthorized(COMPANIES_PATH_V, noHeaders());
    }

    private static AccessTestCase notPublicActuatorEndpointTestCase() {
        return unauthorized(ACTUATOR_ENV_PATH_V, noHeaders());
    }

    private static AccessTestCase pathResemblingHealthTestCase() {
        return unauthorized(HEALTHZ_PATH_V, noHeaders());
    }

    private static AccessTestCase unknownPathTestCase() {
        return unauthorized(UNKNOWN_PATH_V, noHeaders());
    }

    private static AccessTestCase expiredTokenTestCase() {
        return unauthorized(API_ROOT_PATH_V, bearerHeaders(TestJwtIssuer.expiredToken()));
    }

    private static AccessTestCase foreignIssuerTokenTestCase() {
        return unauthorized(API_ROOT_PATH_V, bearerHeaders(TestJwtIssuer.foreignIssuerToken()));
    }

    private static AccessTestCase foreignKeyTokenTestCase() {
        return unauthorized(API_ROOT_PATH_V, bearerHeaders(TestJwtIssuer.foreignKeyToken()));
    }

    private static AccessTestCase malformedTokenTestCase() {
        return unauthorized(API_ROOT_PATH_V, bearerHeaders(MALFORMED_TOKEN_V));
    }

    private static AccessTestCase tokenWithoutBearerPrefixTestCase() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, TestJwtIssuer.validToken());

        return unauthorized(API_ROOT_PATH_V, headers);
    }

    private static AccessTestCase healthWithoutTokenTestCase() {
        return AccessTestCase.builder().path(HEALTH_PATH_V).requestHeaders(noHeaders()).expectedStatus(HttpStatus.OK).build();
    }

    private static AccessTestCase gatewayApiDocsWithoutTokenTestCase() {
        return AccessTestCase.builder().path(GATEWAY_DOCS_PATH_V).requestHeaders(noHeaders()).expectedStatus(HttpStatus.OK).build();
    }

    private static AccessTestCase swaggerUiWithoutTokenTestCase() {
        return AccessTestCase.builder().path(SWAGGER_UI_PATH_V).requestHeaders(noHeaders()).expectedStatus(HttpStatus.FOUND).build();
    }

    private static AccessTestCase jobServiceApiDocsWithoutTokenTestCase() {
        return AccessTestCase.builder()
            .path(JOB_SERVICE_DOCS_PATH_V)
            .requestHeaders(noHeaders())
            .expectedStatus(HttpStatus.OK)
            .expectedDownstreamRequests(1)
            .build();
    }

    private static AccessTestCase companyServiceApiDocsWithoutTokenTestCase() {
        return AccessTestCase.builder()
            .path(COMPANY_SERVICE_DOCS_PATH_V)
            .requestHeaders(noHeaders())
            .expectedStatus(HttpStatus.OK)
            .expectedDownstreamRequests(1)
            .build();
    }

    private static AccessTestCase apiRootWithValidTokenTestCase() {
        return AccessTestCase.builder().path(API_ROOT_PATH_V).requestHeaders(validTokenHeaders()).expectedStatus(HttpStatus.OK).build();
    }

    private static AccessTestCase jobsWithValidTokenTestCase() {
        return AccessTestCase.builder()
            .path(JOBS_PATH_V)
            .requestHeaders(validTokenHeaders())
            .expectedStatus(HttpStatus.OK)
            .expectedDownstreamRequests(1)
            .build();
    }

    private static AccessTestCase marketsWithValidTokenTestCase() {
        return AccessTestCase.builder()
            .path(MARKETS_PATH_V)
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
