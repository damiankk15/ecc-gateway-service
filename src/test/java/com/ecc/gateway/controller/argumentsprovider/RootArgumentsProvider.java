package com.ecc.gateway.controller.argumentsprovider;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.ecc.gateway.controller.RootControllerTest;
import com.ecc.gateway.controller.testcase.RootTestCase;
import com.ecc.gateway.dto.HypermediaResponse;
import com.ecc.gateway.dto.HypermediaResponse.LinkObject;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.springframework.http.HttpStatus;

public class RootArgumentsProvider implements ArgumentsProvider {

    private static final Map<String, String> API_RESOURCES_V = new LinkedHashMap<>();

    static {
        API_RESOURCES_V.put("jobs", "/api/jobs");
        API_RESOURCES_V.put("job", "/api/jobs/{jobId}");
        API_RESOURCES_V.put("companies", "/api/companies");
        API_RESOURCES_V.put("markets", "/api/markets");
    }

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext aContext) {
        return Stream.of(
            // negative test cases
            arguments("Empty api resources", emptyApiResourcesTestCase()),

            // positive test cases
            arguments("Root test case", rootTestCase())
        );
    }

    private RootTestCase emptyApiResourcesTestCase() {
        return new RootTestCase.Builder()
            .withMockedApiResources(Collections.emptyMap())
            .withExpectedStatus(HttpStatus.OK)
            .withExpectedRoot(new HypermediaResponse(Collections.emptyMap()))
            .build();
    }

    private RootTestCase rootTestCase() {
        return new RootTestCase.Builder()
            .withMockedApiResources(API_RESOURCES_V)
            .withExpectedStatus(HttpStatus.OK)
            .withExpectedRoot(new HypermediaResponse(expectedLinks()))
            .build();
    }

    private Map<String, LinkObject> expectedLinks() {
        Map<String, LinkObject> links = new LinkedHashMap<>();
        links.put("jobs", new LinkObject(RootControllerTest.BASE_URL + "/api/jobs"));
        links.put("job", new LinkObject(RootControllerTest.BASE_URL + "/api/jobs/{jobId}", true));
        links.put("companies", new LinkObject(RootControllerTest.BASE_URL + "/api/companies"));
        links.put("markets", new LinkObject(RootControllerTest.BASE_URL + "/api/markets"));
        return links;
    }
}
