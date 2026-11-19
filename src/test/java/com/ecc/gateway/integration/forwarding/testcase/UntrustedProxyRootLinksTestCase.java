package com.ecc.gateway.integration.forwarding.testcase;

import static com.ecc.gateway.integration.support.TestHeaders.LOCAL_HOST_V;
import static com.ecc.gateway.integration.support.TestHeaders.traefikHttpsHeaders;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.json.JsonMapper;

@Builder
public record UntrustedProxyRootLinksTestCase(
    // input
    HttpHeaders requestHeaders,
    // expected results
    HttpStatus expectedStatus,
    String expectedBody
) {
    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

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
            .requestHeaders(headers)
            .expectedStatus(HttpStatus.OK)
            .expectedBody(json(ApiResponse.ok(new HypermediaResponse(applicationYmlLinks(LOCAL_HTTP_BASE_URL_V)))))
            .build();
    }

    private static Map<String, LinkObject> applicationYmlLinks(String baseUrl) {
        Map<String, LinkObject> links = new LinkedHashMap<>();
        links.put("jobs", new LinkObject(baseUrl + "/api/jobs"));
        links.put("job", new LinkObject(baseUrl + "/api/jobs/{jobId}", true));
        links.put("companies", new LinkObject(baseUrl + "/api/companies"));
        links.put("markets", new LinkObject(baseUrl + "/api/markets"));

        return links;
    }

    private static String json(Object value) {
        return JSON_MAPPER.writeValueAsString(value);
    }
}
