package com.ecc.gateway.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.ecc.gateway.config.properties.ApiResources;
import com.ecc.gateway.controller.testcase.RootTestCase;
import com.ecc.gateway.dto.ApiResponse;
import com.ecc.gateway.dto.HypermediaResponse;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RootControllerTest {

    @Mock
    private ApiResources apiResources;

    @InjectMocks
    private RootController rootController;

    @DisplayName("[root] -> Should return hypermedia links built from the request base url and configured api resources in their order")
    @ParameterizedTest(name = "[{index}] -> {argumentSetName}")
    @ArgumentsSource(RootTestCase.ArgumentsProvider.class)
    void shouldReturnHypermediaLinks(RootTestCase testCase) {
        // given
        when(apiResources.getResources()).thenReturn(testCase.mockedApiResources());

        // when
        ApiResponse<HypermediaResponse> response = rootController.root(testCase.request()).block();

        // then
        assertEquals(testCase.expectedResponse(), response);
        assertEquals(List.copyOf(testCase.expectedResponse().data().links().keySet()), List.copyOf(response.data().links().keySet()));
    }
}
