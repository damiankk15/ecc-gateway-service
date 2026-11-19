package com.ecc.gateway.exception.testcase;

import static com.ecc.gateway.support.Outcome.returned;
import static com.ecc.gateway.support.Outcome.thrown;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

import com.ecc.gateway.dto.ApiError;
import com.ecc.gateway.dto.ApiResponse;
import com.ecc.gateway.dto.ApiResponseStatus;
import com.ecc.gateway.support.Outcome;
import io.netty.channel.ConnectTimeoutException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.List;
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
public record DownstreamUnavailableHandleTestCase(
    // input
    ServerWebExchange exchange,
    Throwable exception,
    // expected results
    Outcome<Void> expectedOutcome,
    HttpStatus expectedStatus,
    List<ApiResponse<?>> expectedWrittenBodies
) {
    private static final String JOBS_URL_V = "http://ecc-api.local/api/jobs";
    private static final String CONNECTION_REFUSED_R = "Connection refused: /127.0.0.1:1";
    private static final String CONNECT_TIMEOUT_R = "connection timed out after 3000 ms: /10.0.0.1:8080";
    private static final String UNKNOWN_HOST_R = "Failed to resolve 'job-service' [A(1)]";
    private static final String UNEXPECTED_R = "Unexpected failure";

    private static final ApiResponse<Object> SERVICE_UNAVAILABLE_RESPONSE_V = ApiResponse.error(
        ApiResponseStatus.SERVICE_UNAVAILABLE,
        List.of(new ApiError("SERVICE_UNAVAILABLE", "Service is temporarily unavailable", null))
    );

    public static class ArgumentsProvider implements org.junit.jupiter.params.provider.ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
            return Stream.of(
                // negative test cases
                argumentSet("Unrelated exception", unrelatedExceptionTestCase()),
                argumentSet("Gateway timeout status exception", gatewayTimeoutTestCase()),
                argumentSet("Connect failure after response was committed", committedResponseTestCase()),

                // positive test cases
                argumentSet("Connection refused", connectionRefusedTestCase()),
                argumentSet("Connect timeout", connectTimeoutTestCase()),
                argumentSet("Unknown downstream host", unknownHostTestCase()),
                argumentSet("Connection refused wrapped in another exception", wrappedConnectionRefusedTestCase())
            );
        }
    }

    private static DownstreamUnavailableHandleTestCase unrelatedExceptionTestCase() {
        return DownstreamUnavailableHandleTestCase.builder()
            .exchange(jobsExchange())
            .exception(new IllegalStateException(UNEXPECTED_R))
            .expectedOutcome(thrown(IllegalStateException.class, UNEXPECTED_R))
            .expectedWrittenBodies(List.of())
            .build();
    }

    private static DownstreamUnavailableHandleTestCase gatewayTimeoutTestCase() {
        return DownstreamUnavailableHandleTestCase.builder()
            .exchange(jobsExchange())
            .exception(new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT))
            .expectedOutcome(thrown(ResponseStatusException.class, "504 GATEWAY_TIMEOUT"))
            .expectedWrittenBodies(List.of())
            .build();
    }

    private static DownstreamUnavailableHandleTestCase committedResponseTestCase() {
        ServerWebExchange exchange = jobsExchange();
        exchange.getResponse().setComplete().block();

        return DownstreamUnavailableHandleTestCase.builder()
            .exchange(exchange)
            .exception(new IllegalStateException(UNEXPECTED_R, new ConnectException(CONNECTION_REFUSED_R)))
            .expectedOutcome(thrown(IllegalStateException.class, UNEXPECTED_R))
            .expectedWrittenBodies(List.of())
            .build();
    }

    private static DownstreamUnavailableHandleTestCase connectionRefusedTestCase() {
        return serviceUnavailable(new ConnectException(CONNECTION_REFUSED_R));
    }

    private static DownstreamUnavailableHandleTestCase connectTimeoutTestCase() {
        return serviceUnavailable(new ConnectTimeoutException(CONNECT_TIMEOUT_R));
    }

    private static DownstreamUnavailableHandleTestCase unknownHostTestCase() {
        return serviceUnavailable(new UnknownHostException(UNKNOWN_HOST_R));
    }

    private static DownstreamUnavailableHandleTestCase wrappedConnectionRefusedTestCase() {
        return serviceUnavailable(new IllegalStateException(UNEXPECTED_R, new ConnectException(CONNECTION_REFUSED_R)));
    }

    private static DownstreamUnavailableHandleTestCase serviceUnavailable(Throwable exception) {
        return DownstreamUnavailableHandleTestCase.builder()
            .exchange(jobsExchange())
            .exception(exception)
            .expectedOutcome(returned(null))
            .expectedStatus(HttpStatus.SERVICE_UNAVAILABLE)
            .expectedWrittenBodies(List.of(SERVICE_UNAVAILABLE_RESPONSE_V))
            .build();
    }

    private static ServerWebExchange jobsExchange() {
        return MockServerWebExchange.from(MockServerHttpRequest.get(JOBS_URL_V));
    }
}
