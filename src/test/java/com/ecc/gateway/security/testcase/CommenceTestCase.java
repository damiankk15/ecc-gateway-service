package com.ecc.gateway.security.testcase;

import static org.junit.jupiter.params.provider.Arguments.argumentSet;

import com.ecc.gateway.dto.ApiError;
import com.ecc.gateway.dto.ApiResponse;
import com.ecc.gateway.dto.ApiResponseStatus;
import java.util.List;
import java.util.stream.Stream;
import lombok.Builder;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.support.ParameterDeclarations;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.resource.BearerTokenError;
import org.springframework.web.server.ServerWebExchange;

@Builder
public record CommenceTestCase(
    // input
    ServerWebExchange exchange,
    AuthenticationException exception,
    // expected results
    HttpStatus expectedStatus,
    String expectedWwwAuthenticate,
    List<ApiResponse<?>> expectedWrittenBodies
) {
    private static final String JOBS_URL_V = "http://ecc-api.local/api/jobs";
    private static final String JWT_EXPIRED_R = "Jwt expired at 2026-11-19T10:00:00Z";
    private static final String MALFORMED_REQUEST_R = "Bearer token is malformed";

    private static final ApiResponse<Object> UNAUTHORIZED_RESPONSE_V = ApiResponse.error(
        ApiResponseStatus.BAD_CREDENTIALS,
        List.of(new ApiError("BAD_CREDENTIALS", "Missing or invalid access token", null))
    );

    public static class ArgumentsProvider implements org.junit.jupiter.params.provider.ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
            return Stream.of(
                argumentSet("Missing token", missingTokenTestCase()),
                argumentSet("Invalid token", invalidTokenTestCase()),
                argumentSet("Malformed request", malformedRequestTestCase())
            );
        }
    }

    private static CommenceTestCase missingTokenTestCase() {
        return CommenceTestCase.builder()
            .exchange(jobsExchange())
            .exception(new AuthenticationCredentialsNotFoundException("Not Authenticated"))
            .expectedStatus(HttpStatus.UNAUTHORIZED)
            .expectedWwwAuthenticate("Bearer")
            .expectedWrittenBodies(List.of(UNAUTHORIZED_RESPONSE_V))
            .build();
    }

    private static CommenceTestCase invalidTokenTestCase() {
        return CommenceTestCase.builder()
            .exchange(jobsExchange())
            .exception(new OAuth2AuthenticationException(new BearerTokenError("invalid_token", HttpStatus.UNAUTHORIZED, JWT_EXPIRED_R, null)))
            .expectedStatus(HttpStatus.UNAUTHORIZED)
            .expectedWwwAuthenticate("Bearer error=\"invalid_token\", error_description=\"" + JWT_EXPIRED_R + "\"")
            .expectedWrittenBodies(List.of(UNAUTHORIZED_RESPONSE_V))
            .build();
    }

    private static CommenceTestCase malformedRequestTestCase() {
        return CommenceTestCase.builder()
            .exchange(jobsExchange())
            .exception(new OAuth2AuthenticationException(new BearerTokenError("invalid_request", HttpStatus.BAD_REQUEST, MALFORMED_REQUEST_R, null)))
            .expectedStatus(HttpStatus.BAD_REQUEST)
            .expectedWwwAuthenticate("Bearer error=\"invalid_request\", error_description=\"" + MALFORMED_REQUEST_R + "\"")
            .expectedWrittenBodies(List.of(UNAUTHORIZED_RESPONSE_V))
            .build();
    }

    private static ServerWebExchange jobsExchange() {
        return MockServerWebExchange.from(MockServerHttpRequest.get(JOBS_URL_V));
    }
}
