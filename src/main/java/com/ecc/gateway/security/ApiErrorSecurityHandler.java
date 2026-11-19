package com.ecc.gateway.security;

import com.ecc.gateway.dto.ApiError;
import com.ecc.gateway.dto.ApiResponse;
import com.ecc.gateway.dto.ApiResponseStatus;
import com.ecc.gateway.web.ApiResponseWriter;
import java.util.List;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.access.server.BearerTokenServerAccessDeniedHandler;
import org.springframework.security.oauth2.server.resource.web.server.BearerTokenServerAuthenticationEntryPoint;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Handles requests rejected by Spring Security with an {@link ApiResponse} error body, so a {@code 401 Unauthorized} or {@code 403 Forbidden}
 * response has the same shape as the Gateway's other responses. The status code and the {@code WWW-Authenticate} bearer challenge are still set by
 * Spring Security's {@link BearerTokenServerAuthenticationEntryPoint} and {@link BearerTokenServerAccessDeniedHandler}; this class only replaces the
 * empty body they complete the response with.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
@Component
public class ApiErrorSecurityHandler implements ServerAuthenticationEntryPoint, ServerAccessDeniedHandler {

    private static final ApiResponse<Object> UNAUTHORIZED_RESPONSE = ApiResponse.error(
        ApiResponseStatus.BAD_CREDENTIALS,
        List.of(new ApiError("BAD_CREDENTIALS", "Missing or invalid access token", null))
    );

    private static final ApiResponse<Object> FORBIDDEN_RESPONSE = ApiResponse.error(
        ApiResponseStatus.FORBIDDEN,
        List.of(new ApiError("FORBIDDEN", "Access to this resource is denied", null))
    );

    private final ServerAuthenticationEntryPoint authenticationEntryPoint = new BearerTokenServerAuthenticationEntryPoint();
    private final ServerAccessDeniedHandler accessDeniedHandler = new BearerTokenServerAccessDeniedHandler();
    private final ApiResponseWriter apiResponseWriter;

    /**
     * Creates a new {@link ApiErrorSecurityHandler} that writes response bodies with the given {@link ApiResponseWriter}.
     *
     * @param apiResponseWriter writer used to serialize the {@link ApiResponse} error body
     */
    public ApiErrorSecurityHandler(ApiResponseWriter apiResponseWriter) {
        this.apiResponseWriter = apiResponseWriter;
    }

    /**
     * Rejects an unauthenticated request with the bearer token status and challenge, and a {@link ApiResponseStatus#BAD_CREDENTIALS} body.
     *
     * @param exchange the current exchange
     * @param exception the reason authentication failed
     * @return a {@link Mono} completing when the response is written
     */
    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException exception) {
        return authenticationEntryPoint.commence(withBody(exchange, UNAUTHORIZED_RESPONSE), exception);
    }

    /**
     * Rejects an authenticated request that is not allowed with the bearer token status and challenge, and a {@link ApiResponseStatus#FORBIDDEN}
     * body.
     *
     * @param exchange the current exchange
     * @param exception the reason access was denied
     * @return a {@link Mono} completing when the response is written
     */
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException exception) {
        return accessDeniedHandler.handle(withBody(exchange, FORBIDDEN_RESPONSE), exception);
    }

    /**
     * Returns a copy of {@code exchange} whose response writes {@code body} when it is completed, instead of completing it empty.
     *
     * @param exchange the current exchange
     * @param body the error body to write
     * @return the exchange with the decorated response
     */
    private ServerWebExchange withBody(ServerWebExchange exchange, ApiResponse<Object> body) {
        ServerHttpResponse response = exchange.getResponse();

        ServerHttpResponseDecorator bodyWritingResponse = new ServerHttpResponseDecorator(response) {
            @Override
            public Mono<Void> setComplete() {
                return apiResponseWriter.write(response, body);
            }
        };

        return exchange.mutate().response(bodyWritingResponse).build();
    }
}
