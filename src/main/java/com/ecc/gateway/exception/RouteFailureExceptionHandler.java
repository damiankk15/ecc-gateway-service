package com.ecc.gateway.exception;

import com.ecc.gateway.dto.ApiError;
import com.ecc.gateway.dto.ApiResponse;
import com.ecc.gateway.dto.ApiResponseStatus;
import com.ecc.gateway.web.ApiResponseWriter;
import java.util.List;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

/**
 * {@link WebExceptionHandler} that gives the Gateway's own routing failures an {@link ApiResponse} error body instead of Spring Boot's default
 * error body: a request that matches no route or endpoint ({@code 404 Not Found}) and a downstream service that does not respond within the
 * configured response timeout ({@code 504 Gateway Timeout}). Both reach this handler as a {@link ResponseStatusException}, and their HTTP status is
 * kept. It runs before Spring Boot's {@code ErrorWebExceptionHandler} (order {@code -1}) and passes every other exception, or a response that is
 * already committed, on unchanged.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
@Component
@Order(-2)
public class RouteFailureExceptionHandler implements WebExceptionHandler {

    private static final ApiResponse<Object> NOT_FOUND_RESPONSE = ApiResponse.error(
        ApiResponseStatus.NOT_FOUND_ERROR,
        List.of(new ApiError("NOT_FOUND", "Resource not found", null))
    );

    private static final ApiResponse<Object> GATEWAY_TIMEOUT_RESPONSE = ApiResponse.error(
        ApiResponseStatus.SERVICE_UNAVAILABLE,
        List.of(new ApiError("GATEWAY_TIMEOUT", "Service did not respond in time", null))
    );

    private final ApiResponseWriter apiResponseWriter;

    /**
     * Creates a new {@link RouteFailureExceptionHandler} that writes response bodies with the given {@link ApiResponseWriter}.
     *
     * @param apiResponseWriter writer used to serialize the {@link ApiResponse} error body
     */
    public RouteFailureExceptionHandler(ApiResponseWriter apiResponseWriter) {
        this.apiResponseWriter = apiResponseWriter;
    }

    /**
     * Writes a {@code 404} or {@code 504} {@link ApiResponse} error body when {@code exception} is a {@link ResponseStatusException} with one of
     * these statuses and the response is not yet committed, otherwise passes {@code exception} on to the next handler.
     *
     * @param exchange the current exchange
     * @param exception the exception to handle
     * @return a {@link Mono} completing when the response is written, or failing with {@code exception} when it is not handled here
     */
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable exception) {
        ServerHttpResponse response = exchange.getResponse();

        if (!(exception instanceof ResponseStatusException statusException) || response.isCommitted()) {
            return Mono.error(exception);
        }

        ApiResponse<Object> body = errorBody(statusException.getStatusCode());

        if (body == null) {
            return Mono.error(exception);
        }

        response.setStatusCode(statusException.getStatusCode());

        return apiResponseWriter.write(response, body);
    }

    /**
     * Returns the {@link ApiResponse} error body for a routing failure status.
     *
     * @param status the status of the {@link ResponseStatusException}
     * @return the error body for {@code 404} or {@code 504}, or {@code null} for any other status
     */
    private ApiResponse<Object> errorBody(HttpStatusCode status) {
        return switch (status.value()) {
            case 404 -> NOT_FOUND_RESPONSE;
            case 504 -> GATEWAY_TIMEOUT_RESPONSE;
            default -> null;
        };
    }
}
