package com.ecc.gateway.web.testcase;

import static org.junit.jupiter.params.provider.Arguments.argumentSet;

import com.ecc.gateway.dto.ApiError;
import com.ecc.gateway.dto.ApiResponse;
import com.ecc.gateway.dto.ApiResponseStatus;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;
import lombok.Builder;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.support.ParameterDeclarations;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;

@Builder
public record WriteTestCase(
    // input
    MockServerHttpResponse response,
    ApiResponse<?> body,
    // mocked values
    byte[] mockedBytes,
    // expected results
    MediaType expectedContentType,
    String expectedBody
) {
    private static final String ERROR_JSON_V =
        "{\"status\":\"SERVICE_UNAVAILABLE\",\"data\":null,\"errors\":[{\"code\":\"SERVICE_UNAVAILABLE\",\"message\":\"Service is temporarily unavailable\"}]}";
    private static final String TEXT_JSON_V = "{\"status\":\"OK\",\"data\":\"text\",\"errors\":[]}";

    public static class ArgumentsProvider implements org.junit.jupiter.params.provider.ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
            return Stream.of(
                argumentSet("Error response", errorResponseTestCase()),
                argumentSet("Successful response", successfulResponseTestCase())
            );
        }
    }

    private static WriteTestCase errorResponseTestCase() {
        return WriteTestCase.builder()
            .response(new MockServerHttpResponse())
            .body(
                ApiResponse.error(
                    ApiResponseStatus.SERVICE_UNAVAILABLE,
                    List.of(new ApiError("SERVICE_UNAVAILABLE", "Service is temporarily unavailable", null))
                )
            )
            .mockedBytes(ERROR_JSON_V.getBytes(StandardCharsets.UTF_8))
            .expectedContentType(MediaType.APPLICATION_JSON)
            .expectedBody(ERROR_JSON_V)
            .build();
    }

    private static WriteTestCase successfulResponseTestCase() {
        return WriteTestCase.builder()
            .response(new MockServerHttpResponse())
            .body(ApiResponse.ok("text"))
            .mockedBytes(TEXT_JSON_V.getBytes(StandardCharsets.UTF_8))
            .expectedContentType(MediaType.APPLICATION_JSON)
            .expectedBody(TEXT_JSON_V)
            .build();
    }
}
