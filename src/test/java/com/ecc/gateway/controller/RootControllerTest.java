package com.ecc.gateway.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ecc.gateway.controller.testcase.RootTestCase;
import com.ecc.gateway.dto.ApiResponse;
import com.ecc.gateway.dto.HypermediaResponse;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

class RootControllerTest {

    @DisplayName("[root] -> Should return hypermedia links built from the request base url and configured api resources in their order")
    @ParameterizedTest(name = "[{index}] -> {argumentSetName}")
    @ArgumentsSource(RootTestCase.ArgumentsProvider.class)
    void shouldReturnHypermediaLinks(RootTestCase testCase) {
        // given
        RootController rootController = new RootController(testCase.mockedApiResources());

        // when
        ApiResponse<HypermediaResponse> response = rootController.root(testCase.request()).block();

        // then
        assertEquals(testCase.expectedResponse(), response);
        assertEquals(List.copyOf(testCase.expectedResponse().data().links().entrySet()), List.copyOf(response.data().links().entrySet()));
    }
}
