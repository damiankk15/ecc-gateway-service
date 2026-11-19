package com.ecc.gateway.integration.cors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ecc.gateway.integration.cors.testcase.CorsTestCase;
import com.ecc.gateway.integration.support.AbstractGatewayIntegrationTest;
import com.ecc.gateway.integration.support.stub.DownstreamStubs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.EntityExchangeResult;

class CorsIntegrationTest extends AbstractGatewayIntegrationTest {

    @DisplayName("[cors] -> Should allow cross-origin requests from the UI origin and reject foreign origins or not allowed methods")
    @ParameterizedTest(name = "[{index}] -> {argumentSetName}")
    @ArgumentsSource(CorsTestCase.ArgumentsProvider.class)
    void shouldApplyCorsRules(CorsTestCase testCase) {
        // given

        // when
        EntityExchangeResult<String> response = exchange(testCase.method(), testCase.path(), testCase.requestHeaders());

        // then
        HttpHeaders responseHeaders = response.getResponseHeaders();

        assertEquals(testCase.expectedStatus(), response.getStatus());
        assertEquals(testCase.expectedAllowOrigin(), responseHeaders.getAccessControlAllowOrigin());
        assertEquals(testCase.expectedAllowCredentials(), responseHeaders.getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
        assertEquals(testCase.expectedDownstreamRequests(), DownstreamStubs.receivedRequests());
    }
}
