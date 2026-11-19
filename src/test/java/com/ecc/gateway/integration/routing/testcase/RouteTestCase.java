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
    DownstreamCall expectedDownstreamCall
) {
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
        return notRouted("/api/unknown");
    }

    private static RouteTestCase servicePathWithoutApiPrefixTestCase() {
        return notRouted("/jobs");
    }

    private static RouteTestCase jobListTestCase() {
        return apiRoute("/api/jobs", DownstreamStubs.JOB_SERVICE, "/api/jobs");
    }

    private static RouteTestCase singleJobTestCase() {
        return apiRoute("/api/jobs/42", DownstreamStubs.JOB_SERVICE, "/api/jobs/42");
    }

    private static RouteTestCase jobListWithQueryStringTestCase() {
        return apiRoute("/api/jobs?page=1&size=20", DownstreamStubs.JOB_SERVICE, "/api/jobs?page=1&size=20");
    }

    private static RouteTestCase companyListTestCase() {
        return apiRoute("/api/companies", DownstreamStubs.COMPANY_SERVICE, "/api/companies");
    }

    private static RouteTestCase companyEvaluationTestCase() {
        return apiRoute("/api/companies/1/evaluation?year=2025", DownstreamStubs.COMPANY_SERVICE, "/api/companies/1/evaluation?year=2025");
    }

    private static RouteTestCase marketListTestCase() {
        return apiRoute("/api/markets", DownstreamStubs.COMPANY_SERVICE, "/api/markets");
    }

    private static RouteTestCase jobServiceApiDocsTestCase() {
        return docsRoute("/jobs/v3/api-docs", DownstreamStubs.JOB_SERVICE);
    }

    private static RouteTestCase companyServiceApiDocsTestCase() {
        return docsRoute("/companies/v3/api-docs", DownstreamStubs.COMPANY_SERVICE);
    }

    private static RouteTestCase notRouted(String path) {
        return RouteTestCase.builder()
            .path(path)
            .requestHeaders(publicHostHeaders(TestJwtIssuer.validToken()))
            .expectedStatus(HttpStatus.NOT_FOUND)
            .build();
    }

    private static RouteTestCase apiRoute(String path, StubDownstreamServer service, String downstreamUri) {
        String token = TestJwtIssuer.validToken();

        return RouteTestCase.builder()
            .path(path)
            .requestHeaders(publicHostHeaders(token))
            .expectedStatus(HttpStatus.OK)
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
            .expectedDownstreamCall(DownstreamCall.builder().service(service.name()).uri("/v3/api-docs").host(service.hostAndPort()).build())
            .build();
    }

    private static HttpHeaders publicHostHeaders(String token) {
        HttpHeaders headers = bearerHeaders(token);
        headers.set(HttpHeaders.HOST, PUBLIC_HOST_V);

        return headers;
    }
}
