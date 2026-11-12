package com.ecc.gateway.dto.testcase;

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
public record HypermediaResponseTestCase(
    // input
    Map<String, LinkObject> links,
    // expected results
    HypermediaResponse expectedHypermediaResponse
) {
    private static final LinkObject JOBS_LINK_V = new LinkObject("http://ecc-api.local/api/jobs");
    private static final LinkObject JOB_LINK_V = new LinkObject("http://ecc-api.local/api/jobs/{jobId}", true);
    private static final LinkObject COMPANIES_LINK_V = new LinkObject("http://ecc-api.local/api/companies");
    private static final LinkObject MARKETS_LINK_V = new LinkObject("http://ecc-api.local/api/markets");

    public static class ArgumentsProvider implements org.junit.jupiter.params.provider.ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
            return Stream.of(
                argumentSet("Null links", nullLinksTestCase()),
                argumentSet("Empty links", emptyLinksTestCase()),
                argumentSet("Single link", singleLinkTestCase()),
                argumentSet("Multiple links in given order", multipleLinksTestCase())
            );
        }
    }

    private static HypermediaResponseTestCase nullLinksTestCase() {
        return HypermediaResponseTestCase.builder().links(null).expectedHypermediaResponse(new HypermediaResponse(Map.of())).build();
    }

    private static HypermediaResponseTestCase emptyLinksTestCase() {
        return HypermediaResponseTestCase.builder().links(Map.of()).expectedHypermediaResponse(new HypermediaResponse(Map.of())).build();
    }

    private static HypermediaResponseTestCase singleLinkTestCase() {
        return HypermediaResponseTestCase.builder()
            .links(Map.of("jobs", JOBS_LINK_V))
            .expectedHypermediaResponse(new HypermediaResponse(Map.of("jobs", JOBS_LINK_V)))
            .build();
    }

    private static HypermediaResponseTestCase multipleLinksTestCase() {
        return HypermediaResponseTestCase.builder()
            .links(nonAlphabeticalLinks())
            .expectedHypermediaResponse(new HypermediaResponse(nonAlphabeticalLinks()))
            .build();
    }

    private static Map<String, LinkObject> nonAlphabeticalLinks() {
        Map<String, LinkObject> links = new LinkedHashMap<>();
        links.put("markets", MARKETS_LINK_V);
        links.put("jobs", JOBS_LINK_V);
        links.put("job", JOB_LINK_V);
        links.put("companies", COMPANIES_LINK_V);

        return links;
    }
}
