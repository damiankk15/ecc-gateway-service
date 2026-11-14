package com.ecc.gateway.integration.root;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ecc.gateway.integration.root.testcase.RootLinksTestCase;
import com.ecc.gateway.integration.support.AbstractGatewayIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.reactive.server.EntityExchangeResult;

class RootLinksIntegrationTest extends AbstractGatewayIntegrationTest {

    @DisplayName("[root] -> Should build hypermedia links from the public address and the configured resources")
    @ParameterizedTest(name = "[{index}] -> {argumentSetName}")
    @ArgumentsSource(RootLinksTestCase.ArgumentsProvider.class)
    void shouldBuildRootLinksFromPublicAddress(RootLinksTestCase testCase) {
        // given

        // when
        EntityExchangeResult<String> response = exchange(HttpMethod.GET, "/api", testCase.requestHeaders());

        // then
        assertEquals(testCase.expectedStatus(), response.getStatus());
        assertEquals(testCase.expectedBody(), response.getResponseBody());
    }
}
