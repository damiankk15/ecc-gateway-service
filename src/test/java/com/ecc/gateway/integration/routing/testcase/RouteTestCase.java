package com.ecc.gateway.integration.routing.testcase;

import static com.ecc.gateway.integration.support.TestHeaders.PUBLIC_HOST_V;
import static com.ecc.gateway.integration.support.TestHeaders.bearerHeaders;
import static com.ecc.gateway.integration.support.TestHeaders.noHeaders;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

import com.ecc.gateway.integration.support.jwt.TestJwtIssuer;
import com.ecc.gateway.integration.support.stub.DownstreamCall;
import com.ecc.gateway.integration.support.stub.DownstreamStubs;
import com.ecc.gateway.integration.support.stub.StubDownstreamServer;
import java.util.stream.Stream;
import lombok.Builder;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.support.ParameterDeclarations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

@Builder
public record RouteTestCase(
    // input
    String path,
    HttpHeaders requestHeaders,
    // expected results
    HttpStatus expectedStatus,
    String expectedBody,
    DownstreamCall expectedDownstreamCall
) {
    private static final String UNKNOWN_API_PATH_V = "/api/unknown";
    private static final String JOBS_WITHOUT_API_PREFIX_PATH_V = "/jobs";
    private static final String JOBS_PATH_V = "/api/jobs";
    private static final String JOB_PATH_V = "/api/jobs/42";
    private static final String JOBS_WITH_QUERY_PATH_V = "/api/jobs?page=1&size=20";
    private static final String COMPANIES_PATH_V = "/api/companies";
    private static final String COMPANY_EVALUATION_PATH_V = "/api/companies/1/evaluation?year=2025";
    private static final String MARKETS_PATH_V = "/api/markets";
    private static final String JOB_SERVICE_DOCS_PATH_V = "/jobs/v3/api-docs";
    private static final String COMPANY_SERVICE_DOCS_PATH_V = "/companies/v3/api-docs";
    private static final String DOWNSTREAM_DOCS_PATH_V = "/v3/api-docs";

    private static final String NOT_FOUND_BODY_V =
        "{\"status\":\"NOT_FOUND_ERROR\",\"data\":null,\"errors\":[{\"code\":\"NOT_FOUND\",\"message\":\"Resource not found\"}]}";

    public static class ArgumentsProvider implements org.junit.jupiter.params.provider.ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
            return Stream.of(
                // negative test cases
                argumentSet("Unknown api path", unknownApiPathTestCase()),
                argumentSet("Service path without api prefix", servicePathWithoutApiPrefixTestCase()),

                // positive test cases
                argumentSet("Job list", jobListTestCase()),
                argumentSet("Single job", singleJobTestCase()),
                argumentSet("Job list with query string", jobListWithQueryStringTestCase()),
                argumentSet("Company list", companyListTestCase()),
                argumentSet("Company evaluation with query string", companyEvaluationTestCase()),
                argumentSet("Market list", marketListTestCase()),
                argumentSet("Job service api docs", jobServiceApiDocsTestCase()),
                argumentSet("Company service api docs", companyServiceApiDocsTestCase())
            );
        }
    }

    private static RouteTestCase unknownApiPathTestCase() {
        return notRouted(UNKNOWN_API_PATH_V);
    }

    private static RouteTestCase servicePathWithoutApiPrefixTestCase() {
        return notRouted(JOBS_WITHOUT_API_PREFIX_PATH_V);
    }

    private static RouteTestCase jobListTestCase() {
        return apiRoute(JOBS_PATH_V, DownstreamStubs.JOB_SERVICE, JOBS_PATH_V);
    }

    private static RouteTestCase singleJobTestCase() {
        return apiRoute(JOB_PATH_V, DownstreamStubs.JOB_SERVICE, JOB_PATH_V);
    }

    private static RouteTestCase jobListWithQueryStringTestCase() {
        return apiRoute(JOBS_WITH_QUERY_PATH_V, DownstreamStubs.JOB_SERVICE, JOBS_WITH_QUERY_PATH_V);
    }

    private static RouteTestCase companyListTestCase() {
        return apiRoute(COMPANIES_PATH_V, DownstreamStubs.COMPANY_SERVICE, COMPANIES_PATH_V);
    }

    private static RouteTestCase companyEvaluationTestCase() {
        return apiRoute(COMPANY_EVALUATION_PATH_V, DownstreamStubs.COMPANY_SERVICE, COMPANY_EVALUATION_PATH_V);
    }

    private static RouteTestCase marketListTestCase() {
        return apiRoute(MARKETS_PATH_V, DownstreamStubs.COMPANY_SERVICE, MARKETS_PATH_V);
    }

    private static RouteTestCase jobServiceApiDocsTestCase() {
        return docsRoute(JOB_SERVICE_DOCS_PATH_V, DownstreamStubs.JOB_SERVICE);
    }

    private static RouteTestCase companyServiceApiDocsTestCase() {
        return docsRoute(COMPANY_SERVICE_DOCS_PATH_V, DownstreamStubs.COMPANY_SERVICE);
    }

    private static RouteTestCase notRouted(String path) {
        return RouteTestCase.builder()
            .path(path)
            .requestHeaders(publicHostHeaders(TestJwtIssuer.validToken()))
            .expectedStatus(HttpStatus.NOT_FOUND)
            .expectedBody(NOT_FOUND_BODY_V)
            .build();
    }

    private static RouteTestCase apiRoute(String path, StubDownstreamServer service, String downstreamUri) {
        String token = TestJwtIssuer.validToken();

        return RouteTestCase.builder()
            .path(path)
            .requestHeaders(publicHostHeaders(token))
            .expectedStatus(HttpStatus.OK)
            .expectedBody(service.responseBody())
            .expectedDownstreamCall(
                DownstreamCall.builder()
                    .service(service.name())
                    .uri(downstreamUri)
                    .host(PUBLIC_HOST_V)
                    .authorization("Bearer " + token)
                    .build()
            )
            .build();
    }

    private static RouteTestCase docsRoute(String path, StubDownstreamServer service) {
        return RouteTestCase.builder()
            .path(path)
            .requestHeaders(noHeaders())
            .expectedStatus(HttpStatus.OK)
            .expectedBody(service.responseBody())
            .expectedDownstreamCall(DownstreamCall.builder().service(service.name()).uri(DOWNSTREAM_DOCS_PATH_V).host(service.hostAndPort()).build())
            .build();
    }

    private static HttpHeaders publicHostHeaders(String token) {
        HttpHeaders headers = bearerHeaders(token);
        headers.set(HttpHeaders.HOST, PUBLIC_HOST_V);

        return headers;
    }
}
