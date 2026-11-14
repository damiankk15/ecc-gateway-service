package com.ecc.gateway.integration.support;

import com.ecc.gateway.integration.support.jwt.TestJwtIssuer;
import org.springframework.http.HttpHeaders;

public final class TestHeaders {

    public static final String X_FORWARDED_PROTO_F = "X-Forwarded-Proto";
    public static final String X_FORWARDED_HOST_F = "X-Forwarded-Host";
    public static final String X_FORWARDED_PORT_F = "X-Forwarded-Port";

    public static final String PUBLIC_HOST_V = "ecc-api.dev.local";
    public static final String LOCAL_HOST_V = "ecc-api.local";

    private TestHeaders() {}

    public static HttpHeaders noHeaders() {
        return new HttpHeaders();
    }

    public static HttpHeaders bearerHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    public static HttpHeaders validTokenHeaders() {
        return bearerHeaders(TestJwtIssuer.validToken());
    }

    public static HttpHeaders traefikHttpsHeaders() {
        HttpHeaders headers = validTokenHeaders();
        headers.set(X_FORWARDED_PROTO_F, "https");
        headers.set(X_FORWARDED_HOST_F, PUBLIC_HOST_V);
        headers.set(X_FORWARDED_PORT_F, "443");
        return headers;
    }
}
