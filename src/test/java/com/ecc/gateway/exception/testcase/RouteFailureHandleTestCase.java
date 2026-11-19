package com.ecc.gateway.exception.testcase;

import static com.ecc.gateway.support.Outcome.returned;
import static com.ecc.gateway.support.Outcome.thrown;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

import com.ecc.gateway.dto.ApiError;
import com.ecc.gateway.dto.ApiResponse;
import com.ecc.gateway.dto.ApiResponseStatus;
import com.ecc.gateway.support.Outcome;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;
import lombok.Builder;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.support.ParameterDeclarations;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

@Builder
public record RouteFailureHandleTestCase(
    // input
    ServerWebExchange exchange,
    Throwable exception,
    // expected results
    Outcome<Void> expectedOutcome,
    HttpStatus expectedStatus,
    List<ApiResponse<?>> expectedWrittenBodies
) {
    private static final String UNKNOWN_URL_V = "http://ecc-api.local/api/unknown";
    private static final String JOBS_URL_V = "http://ecc-api.local/api/jobs";
    private static final String UNEXPECTED_R = "Unexpected failure";
    private static final String RESPONSE_TIMEOUT_R = "Response took longer than timeout: PT30S";

    private static final ApiResponse<Object> NOT_FOUND_RESPONSE_V = ApiResponse.error(
        ApiResponseStatus.NOT_FOUND_ERROR,
        List.of(new ApiError("NOT_FOUND", "Resource not found", null))
    );

    private static final ApiResponse<Object> GATEWAY_TIMEOUT_RESPONSE_V = ApiResponse.error(
        ApiResponseStatus.SERVICE_UNAVAILABLE,
        List.of(new ApiError("GATEWAY_TIMEOUT", "Service did not respond in time", null))
    );

    public static class ArgumentsProvider implements org.junit.jupiter.params.provider.ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
            return Stream.of(
                // negative test cases
                argumentSet("Unrelated exception", unrelatedExceptionTestCase()),
                argumentSet("Status exception with another status", otherStatusTestCase()),
                argumentSet("Not found after response was committed", committedResponseTestCase()),

                // positive test cases
                argumentSet("No matching route", notFoundTestCase()),
                argumentSet("Downstream response timeout", gatewayTimeoutTestCase())
            );
        }
    }

    private static RouteFailureHandleTestCase unrelatedExceptionTestCase() {
        return RouteFailureHandleTestCase.builder()
            .exchange(exchange(JOBS_URL_V))
            .exception(new IllegalStateException(UNEXPECTED_R))
            .expectedOutcome(thrown(IllegalStateException.class, UNEXPECTED_R))
            .expectedWrittenBodies(List.of())
            .build();
    }

    private static RouteFailureHandleTestCase otherStatusTestCase() {
        return RouteFailureHandleTestCase.builder()
            .exchange(exchange(JOBS_URL_V))
            .exception(new ResponseStatusException(HttpStatus.BAD_GATEWAY))
            .expectedOutcome(thrown(ResponseStatusException.class, "502 BAD_GATEWAY"))
            .expectedWrittenBodies(List.of())
            .build();
    }

    private static RouteFailureHandleTestCase committedResponseTestCase() {
        ServerWebExchange exchange = exchange(UNKNOWN_URL_V);
        exchange.getResponse().setComplete().block();

        return RouteFailureHandleTestCase.builder()
            .exchange(exchange)
            .exception(new ResponseStatusException(HttpStatus.NOT_FOUND))
            .expectedOutcome(thrown(ResponseStatusException.class, "404 NOT_FOUND"))
            .expectedWrittenBodies(List.of())
            .build();
    }

    private static RouteFailureHandleTestCase notFoundTestCase() {
        return RouteFailureHandleTestCase.builder()
            .exchange(exchange(UNKNOWN_URL_V))
            .exception(new ResponseStatusException(HttpStatus.NOT_FOUND))
            .expectedOutcome(returned(null))
            .expectedStatus(HttpStatus.NOT_FOUND)
            .expectedWrittenBodies(List.of(NOT_FOUND_RESPONSE_V))
            .build();
    }

    private static RouteFailureHandleTestCase gatewayTimeoutTestCase() {
        return RouteFailureHandleTestCase.builder()
            .exchange(exchange(JOBS_URL_V))
            .exception(new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, RESPONSE_TIMEOUT_R, new TimeoutException(RESPONSE_TIMEOUT_R)))
            .expectedOutcome(returned(null))
            .expectedStatus(HttpStatus.GATEWAY_TIMEOUT)
            .expectedWrittenBodies(List.of(GATEWAY_TIMEOUT_RESPONSE_V))
            .build();
    }

    private static ServerWebExchange exchange(String url) {
        return MockServerWebExchange.from(MockServerHttpRequest.get(url));
    }
}
