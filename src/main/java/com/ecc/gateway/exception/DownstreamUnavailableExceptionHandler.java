package com.ecc.gateway.exception;

import com.ecc.gateway.dto.ApiError;
import com.ecc.gateway.dto.ApiResponse;
import com.ecc.gateway.dto.ApiResponseStatus;
import com.ecc.gateway.web.ApiResponseWriter;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.List;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

/**
 * {@link WebExceptionHandler} that turns a failed connection to a downstream service into a {@code 503 Service Unavailable} response with an
 * {@link ApiResponse} error body, instead of the generic {@code 500} produced by Spring Boot's default error handler. A connection failure is a
 * {@link ConnectException} (connection refused or connect timeout, e.g. a service without running pods) or an {@link UnknownHostException} (the
 * service host name does not resolve, e.g. a removed Kubernetes service), thrown directly or found in the cause chain. It runs before Spring Boot's
 * {@code ErrorWebExceptionHandler} (order {@code -1}) and passes every other exception, or a response that is already committed, on unchanged.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
@Component
@Order(-2)
public class DownstreamUnavailableExceptionHandler implements WebExceptionHandler {

    private static final ApiError SERVICE_UNAVAILABLE_ERROR = new ApiError("SERVICE_UNAVAILABLE", "Service is temporarily unavailable", null);

    private final ApiResponseWriter apiResponseWriter;

    /**
     * Creates a new {@link DownstreamUnavailableExceptionHandler} that writes response bodies with the given {@link ApiResponseWriter}.
     *
     * @param apiResponseWriter writer used to serialize the {@link ApiResponse} error body
     */
    public DownstreamUnavailableExceptionHandler(ApiResponseWriter apiResponseWriter) {
        this.apiResponseWriter = apiResponseWriter;
    }

    /**
     * Writes a {@code 503} {@link ApiResponse} error body when {@code exception} is a downstream connection failure and the response is not yet
     * committed, otherwise passes {@code exception} on to the next handler.
     *
     * @param exchange the current exchange
     * @param exception the exception to handle
     * @return a {@link Mono} completing when the response is written, or failing with {@code exception} when it is not handled here
     */
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable exception) {
        ServerHttpResponse response = exchange.getResponse();

        if (!isDownstreamUnreachable(exception) || response.isCommitted()) {
            return Mono.error(exception);
        }

        response.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);

        return apiResponseWriter.write(response, ApiResponse.error(ApiResponseStatus.SERVICE_UNAVAILABLE, List.of(SERVICE_UNAVAILABLE_ERROR)));
    }

    /**
     * Checks whether {@code exception} or any exception in its cause chain means the downstream service cannot be reached: a {@link ConnectException}
     * (connection refused or connect timeout, e.g. a service without running pods) or an {@link UnknownHostException} (the service host name does not
     * resolve, e.g. a removed Kubernetes service).
     *
     * @param exception the exception to inspect, possibly wrapping the actual cause
     * @return {@code true} if the downstream service is unreachable, {@code false} otherwise
     */
    private boolean isDownstreamUnreachable(Throwable exception) {
        for (Throwable current = exception; current != null; current = current.getCause()) {
            if (current instanceof ConnectException || current instanceof UnknownHostException) {
                return true;
            }
        }

        return false;
    }
}
