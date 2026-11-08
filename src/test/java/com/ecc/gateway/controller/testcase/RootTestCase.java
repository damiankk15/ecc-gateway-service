package com.ecc.gateway.controller.testcase;

import static org.junit.jupiter.params.provider.Arguments.argumentSet;

import com.ecc.gateway.dto.ApiResponse;
import com.ecc.gateway.dto.HypermediaResponse;
import com.ecc.gateway.dto.HypermediaResponse.LinkObject;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;
import lombok.Builder;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.support.ParameterDeclarations;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;

@Builder
public record RootTestCase(
    // input
    ServerHttpRequest request,
    // mocked values
    Map<String, String> mockedApiResources,
    // expected results
    ApiResponse<HypermediaResponse> expectedResponse
) {
    private static final String LOCAL_BASE_URL_V = "http://ecc-api.local";
    private static final String DEV_HTTPS_BASE_URL_V = "https://ecc-api.dev.local";
    private static final String LOCALHOST_WITH_PORT_BASE_URL_V = "http://localhost:8080";

    private static final String ROOT_PATH_V = "/api";
    private static final String ROOT_PATH_WITH_QUERY_V = "/api?page=1&size=20";

    private static final String JOBS_PATH_V = "/api/jobs";
    private static final String JOB_PATH_V = "/api/jobs/{jobId}";
    private static final String COMPANIES_PATH_V = "/api/companies";
    private static final String MARKETS_PATH_V = "/api/markets";
    private static final String JOB_LOG_PATH_V = "/api/jobs/{jobId}/logs/{logId}";

    public static class ArgumentsProvider implements org.junit.jupiter.params.provider.ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
            return Stream.of(
                argumentSet("No configured resources", noConfiguredResourcesTestCase()),
                argumentSet("Single plain resource", singlePlainResourceTestCase()),
                argumentSet("Single templated resource", singleTemplatedResourceTestCase()),
                argumentSet("Resource with multiple template variables", multipleTemplateVariablesTestCase()),
                argumentSet("All gateway resources in configured order", allGatewayResourcesTestCase()),
                argumentSet("HTTPS public address", httpsPublicAddressTestCase()),
                argumentSet("Address with non-default port", nonDefaultPortTestCase()),
                argumentSet("Request with query string", requestWithQueryStringTestCase())
            );
        }
    }

    private static RootTestCase noConfiguredResourcesTestCase() {
        return RootTestCase.builder()
            .request(MockServerHttpRequest.get(LOCAL_BASE_URL_V + ROOT_PATH_V).build())
            .mockedApiResources(Map.of())
            .expectedResponse(ApiResponse.ok(new HypermediaResponse(Map.of())))
            .build();
    }

    private static RootTestCase singlePlainResourceTestCase() {
        return RootTestCase.builder()
            .request(MockServerHttpRequest.get(LOCAL_BASE_URL_V + ROOT_PATH_V).build())
            .mockedApiResources(Map.of("jobs", JOBS_PATH_V))
            .expectedResponse(ApiResponse.ok(new HypermediaResponse(Map.of("jobs", new LinkObject(LOCAL_BASE_URL_V + JOBS_PATH_V)))))
            .build();
    }

    private static RootTestCase singleTemplatedResourceTestCase() {
        return RootTestCase.builder()
            .request(MockServerHttpRequest.get(LOCAL_BASE_URL_V + ROOT_PATH_V).build())
            .mockedApiResources(Map.of("job", JOB_PATH_V))
            .expectedResponse(ApiResponse.ok(new HypermediaResponse(Map.of("job", new LinkObject(LOCAL_BASE_URL_V + JOB_PATH_V, true)))))
            .build();
    }

    private static RootTestCase multipleTemplateVariablesTestCase() {
        return RootTestCase.builder()
            .request(MockServerHttpRequest.get(LOCAL_BASE_URL_V + ROOT_PATH_V).build())
            .mockedApiResources(Map.of("jobLog", JOB_LOG_PATH_V))
            .expectedResponse(ApiResponse.ok(new HypermediaResponse(Map.of("jobLog", new LinkObject(LOCAL_BASE_URL_V + JOB_LOG_PATH_V, true)))))
            .build();
    }

    private static RootTestCase allGatewayResourcesTestCase() {
        return RootTestCase.builder()
            .request(MockServerHttpRequest.get(LOCAL_BASE_URL_V + ROOT_PATH_V).build())
            .mockedApiResources(gatewayResources())
            .expectedResponse(ApiResponse.ok(new HypermediaResponse(gatewayLinks(LOCAL_BASE_URL_V))))
            .build();
    }

    private static RootTestCase httpsPublicAddressTestCase() {
        return RootTestCase.builder()
            .request(MockServerHttpRequest.get(DEV_HTTPS_BASE_URL_V + ROOT_PATH_V).build())
            .mockedApiResources(gatewayResources())
            .expectedResponse(ApiResponse.ok(new HypermediaResponse(gatewayLinks(DEV_HTTPS_BASE_URL_V))))
            .build();
    }

    private static RootTestCase nonDefaultPortTestCase() {
        return RootTestCase.builder()
            .request(MockServerHttpRequest.get(LOCALHOST_WITH_PORT_BASE_URL_V + ROOT_PATH_V).build())
            .mockedApiResources(gatewayResources())
            .expectedResponse(ApiResponse.ok(new HypermediaResponse(gatewayLinks(LOCALHOST_WITH_PORT_BASE_URL_V))))
            .build();
    }

    private static RootTestCase requestWithQueryStringTestCase() {
        return RootTestCase.builder()
            .request(MockServerHttpRequest.get(LOCAL_BASE_URL_V + ROOT_PATH_WITH_QUERY_V).build())
            .mockedApiResources(gatewayResources())
            .expectedResponse(ApiResponse.ok(new HypermediaResponse(gatewayLinks(LOCAL_BASE_URL_V))))
            .build();
    }

    private static Map<String, String> gatewayResources() {
        Map<String, String> resources = new LinkedHashMap<>();
        resources.put("jobs", JOBS_PATH_V);
        resources.put("job", JOB_PATH_V);
        resources.put("companies", COMPANIES_PATH_V);
        resources.put("markets", MARKETS_PATH_V);

        return resources;
    }

    private static Map<String, LinkObject> gatewayLinks(String baseUrl) {
        Map<String, LinkObject> links = new LinkedHashMap<>();
        links.put("jobs", new LinkObject(baseUrl + JOBS_PATH_V));
        links.put("job", new LinkObject(baseUrl + JOB_PATH_V, true));
        links.put("companies", new LinkObject(baseUrl + COMPANIES_PATH_V));
        links.put("markets", new LinkObject(baseUrl + MARKETS_PATH_V));

        return links;
    }
}
