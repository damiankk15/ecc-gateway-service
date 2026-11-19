package com.ecc.gateway.integration.configuration.testcase;

import static org.junit.jupiter.params.provider.Arguments.argumentSet;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.Builder;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.support.ParameterDeclarations;

@Builder
public record ApiResourcesBindTestCase(
    // input
    List<String> propertyValues,
    // expected results
    Map<String, String> expectedResources
) {
    public static class ArgumentsProvider implements org.junit.jupiter.params.provider.ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
            return Stream.of(
                argumentSet("No configured resources", noConfiguredResourcesTestCase()),
                argumentSet("Single plain resource", singlePlainResourceTestCase()),
                argumentSet("Templated resource path kept literally", templatedResourceTestCase()),
                argumentSet("Multiple resources in declared order", multipleResourcesTestCase())
            );
        }
    }

    private static ApiResourcesBindTestCase noConfiguredResourcesTestCase() {
        return ApiResourcesBindTestCase.builder().propertyValues(List.of()).expectedResources(Map.of()).build();
    }

    private static ApiResourcesBindTestCase singlePlainResourceTestCase() {
        return ApiResourcesBindTestCase.builder()
            .propertyValues(List.of("api.resources.jobs=/api/jobs"))
            .expectedResources(Map.of("jobs", "/api/jobs"))
            .build();
    }

    private static ApiResourcesBindTestCase templatedResourceTestCase() {
        return ApiResourcesBindTestCase.builder()
            .propertyValues(List.of("api.resources.job=/api/jobs/{jobId}"))
            .expectedResources(Map.of("job", "/api/jobs/{jobId}"))
            .build();
    }

    private static ApiResourcesBindTestCase multipleResourcesTestCase() {
        Map<String, String> expectedResources = new LinkedHashMap<>();
        expectedResources.put("markets", "/api/markets");
        expectedResources.put("jobs", "/api/jobs");
        expectedResources.put("companies", "/api/companies");

        return ApiResourcesBindTestCase.builder()
            .propertyValues(List.of("api.resources.markets=/api/markets", "api.resources.jobs=/api/jobs", "api.resources.companies=/api/companies"))
            .expectedResources(expectedResources)
            .build();
    }
}
