package com.ecc.gateway.integration.routing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ecc.gateway.integration.routing.testcase.DownstreamTimeoutTestCase;
import com.ecc.gateway.integration.support.GatewayTestProperties;
import com.ecc.gateway.integration.support.stub.StubDownstreamServer;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "spring.cloud.gateway.server.webflux.httpclient.response-timeout=500ms")
class DownstreamTimeoutIntegrationTest {

    private static final StubDownstreamServer SLOW_SERVICE = StubDownstreamServer.start("slow-service", "{}", Duration.ofSeconds(3));

    @Value("${local.server.port}")
    private int port;

    private WebTestClient webTestClient;

    @DynamicPropertySource
    static void registerGatewayProperties(DynamicPropertyRegistry registry) {
        GatewayTestProperties.register(registry, SLOW_SERVICE.baseUrl(), SLOW_SERVICE.baseUrl());
    }

    @BeforeEach
    void setup() {
        webTestClient = GatewayTestProperties.webTestClient(port);
    }

    @DisplayName("[routes] -> Should return gateway timeout when the downstream service responds slower than the response timeout")
    @ParameterizedTest(name = "[{index}] -> {argumentSetName}")
    @ArgumentsSource(DownstreamTimeoutTestCase.ArgumentsProvider.class)
    void shouldReturnGatewayTimeout(DownstreamTimeoutTestCase testCase) {
        // given

        // when
        EntityExchangeResult<byte[]> response = webTestClient
            .get()
            .uri(testCase.path())
            .headers(headers -> headers.addAll(testCase.requestHeaders()))
            .exchange()
            .expectBody()
            .returnResult();

        // then
        assertEquals(testCase.expectedStatus(), response.getStatus());
    }
}
