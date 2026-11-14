package com.ecc.gateway.integration.support;

import com.ecc.gateway.integration.support.jwt.TestJwtIssuer;
import com.ecc.gateway.integration.support.stub.DownstreamStubs;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.web.reactive.server.WebTestClient;

public final class GatewayTestProperties {

    public static final String ALLOWED_ORIGIN_V = "https://ecc.dev.local";

    private GatewayTestProperties() {}

    public static void register(DynamicPropertyRegistry registry, String jobServiceUri, String companyServiceUri) {
        registry.add("JOB_SERVICE_URI", () -> jobServiceUri);
        registry.add("COMPANY_SERVICE_URI", () -> companyServiceUri);
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", () -> DownstreamStubs.KEYCLOAK.baseUrl() + TestJwtIssuer.JWKS_PATH_V);
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> TestJwtIssuer.ISSUER_URI_V);
        registry.add("api.cors.allowed-origins", () -> ALLOWED_ORIGIN_V);
    }

    public static WebTestClient webTestClient(int port) {
        return WebTestClient.bindToServer()
            .baseUrl("http://127.0.0.1:" + port)
            .build();
    }
}
