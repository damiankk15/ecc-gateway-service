package com.ecc.gateway.security.testcase;

import static org.junit.jupiter.params.provider.Arguments.argumentSet;

import com.ecc.gateway.dto.ApiError;
import com.ecc.gateway.dto.ApiResponse;
import com.ecc.gateway.dto.ApiResponseStatus;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;
import lombok.Builder;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.support.ParameterDeclarations;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ServerWebExchange;

@Builder
public record HandleTestCase(
    // input
    ServerWebExchange exchange,
    AccessDeniedException exception,
    // expected results
    HttpStatus expectedStatus,
    String expectedWwwAuthenticate,
    List<ApiResponse<?>> expectedWrittenBodies
) {
    private static final String JOBS_URL_V = "http://ecc-api.local/api/jobs";
    private static final String ACCESS_DENIED_R = "Access Denied";

    private static final ApiResponse<Object> FORBIDDEN_RESPONSE_V = ApiResponse.error(
        ApiResponseStatus.FORBIDDEN,
        List.of(new ApiError("FORBIDDEN", "Access to this resource is denied", null))
    );

    public static class ArgumentsProvider implements org.junit.jupiter.params.provider.ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
            return Stream.of(
                argumentSet("Access denied without bearer token authentication", withoutTokenAuthenticationTestCase()),
                argumentSet("Access denied for bearer token with insufficient scope", insufficientScopeTestCase())
            );
        }
    }

    private static HandleTestCase withoutTokenAuthenticationTestCase() {
        return HandleTestCase.builder()
            .exchange(MockServerWebExchange.from(MockServerHttpRequest.get(JOBS_URL_V)))
            .exception(new AccessDeniedException(ACCESS_DENIED_R))
            .expectedStatus(HttpStatus.FORBIDDEN)
            .expectedWwwAuthenticate("Bearer")
            .expectedWrittenBodies(List.of(FORBIDDEN_RESPONSE_V))
            .build();
    }

    private static HandleTestCase insufficientScopeTestCase() {
        return HandleTestCase.builder()
            .exchange(MockServerWebExchange.builder(MockServerHttpRequest.get(JOBS_URL_V)).principal(jwtAuthentication()).build())
            .exception(new AccessDeniedException(ACCESS_DENIED_R))
            .expectedStatus(HttpStatus.FORBIDDEN)
            .expectedWwwAuthenticate(
                "Bearer error=\"insufficient_scope\", error_description=\"The request requires higher privileges than provided by the access token.\", " +
                    "error_uri=\"https://tools.ietf.org/html/rfc6750#section-3.1\""
            )
            .expectedWrittenBodies(List.of(FORBIDDEN_RESPONSE_V))
            .build();
    }

    private static JwtAuthenticationToken jwtAuthentication() {
        Jwt jwt = Jwt.withTokenValue("token")
            .header("alg", "RS256")
            .subject("test-user")
            .issuedAt(Instant.parse("2026-11-19T10:00:00Z"))
            .expiresAt(Instant.parse("2026-11-19T10:05:00Z"))
            .build();

        return new JwtAuthenticationToken(jwt);
    }
}
