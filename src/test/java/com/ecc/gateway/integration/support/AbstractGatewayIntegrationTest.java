package com.ecc.gateway.integration.support;

import com.ecc.gateway.integration.support.stub.DownstreamStubs;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "spring.cloud.gateway.server.webflux.trusted-proxies=127\\.0\\.0\\.1")
public abstract class AbstractGatewayIntegrationTest {

    @Value("${local.server.port}")
    private int port;

    protected WebTestClient webTestClient;

    @DynamicPropertySource
    static void registerGatewayProperties(DynamicPropertyRegistry registry) {
        GatewayTestProperties.register(registry, DownstreamStubs.JOB_SERVICE.baseUrl(), DownstreamStubs.COMPANY_SERVICE.baseUrl());
    }

    @BeforeEach
    void setupGatewayClient() {
        DownstreamStubs.reset();
        webTestClient = GatewayTestProperties.webTestClient(port);
    }

    protected EntityExchangeResult<String> exchange(HttpMethod method, String path, HttpHeaders requestHeaders) {
        return webTestClient
            .method(method)
            .uri(path)
            .headers(headers -> headers.addAll(requestHeaders))
            .exchange()
            .expectBody(String.class)
            .returnResult();
    }
}
