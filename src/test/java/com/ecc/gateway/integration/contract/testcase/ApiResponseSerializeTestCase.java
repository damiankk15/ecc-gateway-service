package com.ecc.gateway.integration.contract.testcase;

import static org.junit.jupiter.params.provider.Arguments.argumentSet;

import com.ecc.gateway.dto.ApiError;
import com.ecc.gateway.dto.ApiResponse;
import com.ecc.gateway.dto.ApiResponseStatus;
import com.ecc.gateway.dto.HypermediaResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.Builder;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.support.ParameterDeclarations;

@Builder
public record ApiResponseSerializeTestCase(
    // input
    ApiResponse<?> response,
    // expected results
    String expectedJson
) {
    private static final ApiError NOT_FOUND_ERROR_V = new ApiError("NOT_FOUND", "Job 42 does not exist", null);
    private static final ApiError VALIDATION_ERROR_V = new ApiError("VALIDATION_ERROR", "must not be blank", "name");

    public static class ArgumentsProvider implements org.junit.jupiter.params.provider.ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
            return Stream.of(
                argumentSet("Successful response with hypermedia payload", successfulHypermediaResponseTestCase()),
                argumentSet("Successful response with text payload", successfulTextResponseTestCase()),
                argumentSet("Error without field", errorWithoutFieldTestCase()),
                argumentSet("Validation error with field", validationErrorWithFieldTestCase())
            );
        }
    }

    private static ApiResponseSerializeTestCase successfulHypermediaResponseTestCase() {
        return ApiResponseSerializeTestCase.builder()
            .response(ApiResponse.ok(new HypermediaResponse(Map.of())))
            .expectedJson("{\"status\":\"OK\",\"data\":{\"_links\":{}},\"errors\":[]}")
            .build();
    }

    private static ApiResponseSerializeTestCase successfulTextResponseTestCase() {
        return ApiResponseSerializeTestCase.builder()
            .response(ApiResponse.ok("text"))
            .expectedJson("{\"status\":\"OK\",\"data\":\"text\",\"errors\":[]}")
            .build();
    }

    private static ApiResponseSerializeTestCase errorWithoutFieldTestCase() {
        return ApiResponseSerializeTestCase.builder()
            .response(ApiResponse.error(ApiResponseStatus.NOT_FOUND_ERROR, List.of(NOT_FOUND_ERROR_V)))
            .expectedJson(
                "{\"status\":\"NOT_FOUND_ERROR\",\"data\":null,\"errors\":[{\"code\":\"NOT_FOUND\",\"message\":\"Job 42 does not exist\"}]}"
            )
            .build();
    }

    private static ApiResponseSerializeTestCase validationErrorWithFieldTestCase() {
        return ApiResponseSerializeTestCase.builder()
            .response(ApiResponse.error(ApiResponseStatus.VALIDATION_ERROR, List.of(VALIDATION_ERROR_V)))
            .expectedJson(
                "{\"status\":\"VALIDATION_ERROR\",\"data\":null,\"errors\":[{\"code\":\"VALIDATION_ERROR\",\"message\":\"must not be blank\",\"field\":\"name\"}]}"
            )
            .build();
    }
}
