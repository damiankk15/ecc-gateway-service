package com.ecc.gateway.integration.contract.testcase;

import static org.junit.jupiter.params.provider.Arguments.argumentSet;

import com.ecc.gateway.dto.HypermediaResponse;
import com.ecc.gateway.dto.HypermediaResponse.LinkObject;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;
import lombok.Builder;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.support.ParameterDeclarations;

@Builder
public record HypermediaResponseSerializeTestCase(
    // input
    HypermediaResponse response,
    // expected results
    String expectedJson
) {
    private static final String BASE_URL_V = "http://ecc-api.local";

    private static final LinkObject JOBS_LINK_V = new LinkObject(BASE_URL_V + "/api/jobs");
    private static final LinkObject JOB_LINK_V = new LinkObject(BASE_URL_V + "/api/jobs/{jobId}", true);
    private static final LinkObject COMPANIES_LINK_V = new LinkObject(BASE_URL_V + "/api/companies");
    private static final LinkObject MARKETS_LINK_V = new LinkObject(BASE_URL_V + "/api/markets");

    private static final String JOBS_JSON_V = "\"jobs\":{\"href\":\"" + BASE_URL_V + "/api/jobs\"}";
    private static final String JOB_JSON_V = "\"job\":{\"href\":\"" + BASE_URL_V + "/api/jobs/{jobId}\",\"templated\":true}";
    private static final String COMPANIES_JSON_V = "\"companies\":{\"href\":\"" + BASE_URL_V + "/api/companies\"}";
    private static final String MARKETS_JSON_V = "\"markets\":{\"href\":\"" + BASE_URL_V + "/api/markets\"}";

    public static class ArgumentsProvider implements org.junit.jupiter.params.provider.ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
            return Stream.of(
                argumentSet("No links", noLinksTestCase()),
                argumentSet("Plain link without templated flag", plainLinkTestCase()),
                argumentSet("Plain link with explicit templated false", explicitNotTemplatedLinkTestCase()),
                argumentSet("Templated link", templatedLinkTestCase()),
                argumentSet("Multiple links in configured order", multipleLinksTestCase())
            );
        }
    }

    private static HypermediaResponseSerializeTestCase noLinksTestCase() {
        return HypermediaResponseSerializeTestCase.builder().response(new HypermediaResponse(Map.of())).expectedJson("{\"_links\":{}}").build();
    }

    private static HypermediaResponseSerializeTestCase plainLinkTestCase() {
        return HypermediaResponseSerializeTestCase.builder()
            .response(new HypermediaResponse(Map.of("jobs", JOBS_LINK_V)))
            .expectedJson("{\"_links\":{" + JOBS_JSON_V + "}}")
            .build();
    }

    private static HypermediaResponseSerializeTestCase explicitNotTemplatedLinkTestCase() {
        return HypermediaResponseSerializeTestCase.builder()
            .response(new HypermediaResponse(Map.of("jobs", new LinkObject(BASE_URL_V + "/api/jobs", false))))
            .expectedJson("{\"_links\":{" + JOBS_JSON_V + "}}")
            .build();
    }

    private static HypermediaResponseSerializeTestCase templatedLinkTestCase() {
        return HypermediaResponseSerializeTestCase.builder()
            .response(new HypermediaResponse(Map.of("job", JOB_LINK_V)))
            .expectedJson("{\"_links\":{" + JOB_JSON_V + "}}")
            .build();
    }

    private static HypermediaResponseSerializeTestCase multipleLinksTestCase() {
        Map<String, LinkObject> links = new LinkedHashMap<>();
        links.put("markets", MARKETS_LINK_V);
        links.put("jobs", JOBS_LINK_V);
        links.put("job", JOB_LINK_V);
        links.put("companies", COMPANIES_LINK_V);

        return HypermediaResponseSerializeTestCase.builder()
            .response(new HypermediaResponse(links))
            .expectedJson("{\"_links\":{" + MARKETS_JSON_V + "," + JOBS_JSON_V + "," + JOB_JSON_V + "," + COMPANIES_JSON_V + "}}")
            .build();
    }
}
