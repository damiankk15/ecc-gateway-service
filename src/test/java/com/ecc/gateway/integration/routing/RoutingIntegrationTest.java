package com.ecc.gateway.integration.routing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ecc.gateway.integration.routing.testcase.RouteTestCase;
import com.ecc.gateway.integration.support.AbstractGatewayIntegrationTest;
import com.ecc.gateway.integration.support.stub.DownstreamStubs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.reactive.server.EntityExchangeResult;

class RoutingIntegrationTest extends AbstractGatewayIntegrationTest {

    @DisplayName("[routes] -> Should route request to the correct downstream service with the expected path, host and authorization")
    @ParameterizedTest(name = "[{index}] -> {argumentSetName}")
    @ArgumentsSource(RouteTestCase.ArgumentsProvider.class)
    void shouldRouteRequestToDownstreamService(RouteTestCase testCase) {
        // given

        // when
        EntityExchangeResult<String> response = exchange(HttpMethod.GET, testCase.path(), testCase.requestHeaders());

        // then
        assertEquals(testCase.expectedStatus(), response.getStatus());
        assertEquals(testCase.expectedBody(), response.getResponseBody());
        assertEquals(testCase.expectedDownstreamCall(), DownstreamStubs.lastCall());
    }
}
