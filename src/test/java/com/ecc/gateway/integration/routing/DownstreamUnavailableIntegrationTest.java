package com.ecc.gateway.integration.routing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ecc.gateway.integration.routing.testcase.DownstreamUnavailableTestCase;
import com.ecc.gateway.integration.support.GatewayTestProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DownstreamUnavailableIntegrationTest {

    private static final String CLOSED_PORT_URI_V = "http://127.0.0.1:1";
    private static final String UNRESOLVABLE_HOST_URI_V = "http://company-service.invalid:8080";

    @Value("${local.server.port}")
    private int port;

    private WebTestClient webTestClient;

    @DynamicPropertySource
    static void registerGatewayProperties(DynamicPropertyRegistry registry) {
        GatewayTestProperties.register(registry, CLOSED_PORT_URI_V, UNRESOLVABLE_HOST_URI_V);
    }

    @BeforeEach
    void setup() {
        webTestClient = GatewayTestProperties.webTestClient(port);
    }

    @DisplayName("[routes] -> Should return service unavailable when the downstream service cannot be reached")
    @ParameterizedTest(name = "[{index}] -> {argumentSetName}")
    @ArgumentsSource(DownstreamUnavailableTestCase.ArgumentsProvider.class)
    void shouldReturnServiceUnavailable(DownstreamUnavailableTestCase testCase) {
        // given

        // when
        EntityExchangeResult<String> response = GatewayTestProperties.exchange(
            webTestClient,
            HttpMethod.GET,
            testCase.path(),
            testCase.requestHeaders()
        );

        // then
        assertEquals(testCase.expectedStatus(), response.getStatus());
        assertEquals(testCase.expectedBody(), response.getResponseBody());
    }
}
