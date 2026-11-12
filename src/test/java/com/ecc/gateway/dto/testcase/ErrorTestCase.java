package com.ecc.gateway.dto.testcase;

import static com.ecc.gateway.support.Outcome.returned;
import static com.ecc.gateway.support.Outcome.thrown;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

import com.ecc.gateway.dto.ApiError;
import com.ecc.gateway.dto.ApiResponse;
import com.ecc.gateway.dto.ApiResponseStatus;
import com.ecc.gateway.support.Outcome;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import lombok.Builder;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.support.ParameterDeclarations;

@Builder
public record ErrorTestCase(
    // input
    ApiResponseStatus status,
    List<ApiError> errors,
    // expected results
    Outcome<ApiResponse<Object>> expectedApiResponse
) {
    private static final ApiError NOT_FOUND_ERROR_V = new ApiError("NOT_FOUND", "Job 42 does not exist", null);
    private static final ApiError VALIDATION_ERROR_V = new ApiError("VALIDATION_ERROR", "must not be blank", "name");

    public static class ArgumentsProvider implements org.junit.jupiter.params.provider.ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
            return Stream.of(
                // negative test cases
                argumentSet("Null error element", nullErrorElementTestCase()),

                // positive test cases
                argumentSet("Not found error", notFoundErrorTestCase()),
                argumentSet("Validation error with field", validationErrorTestCase()),
                argumentSet("Multiple errors", multipleErrorsTestCase()),
                argumentSet("Null errors", nullErrorsTestCase())
            );
        }
    }

    private static ErrorTestCase nullErrorElementTestCase() {
        return ErrorTestCase.builder()
            .status(ApiResponseStatus.NOT_FOUND_ERROR)
            .errors(Arrays.asList(NOT_FOUND_ERROR_V, null))
            .expectedApiResponse(thrown(NullPointerException.class))
            .build();
    }

    private static ErrorTestCase notFoundErrorTestCase() {
        return ErrorTestCase.builder()
            .status(ApiResponseStatus.NOT_FOUND_ERROR)
            .errors(List.of(NOT_FOUND_ERROR_V))
            .expectedApiResponse(returned(new ApiResponse<>(ApiResponseStatus.NOT_FOUND_ERROR, null, List.of(NOT_FOUND_ERROR_V))))
            .build();
    }

    private static ErrorTestCase validationErrorTestCase() {
        return ErrorTestCase.builder()
            .status(ApiResponseStatus.VALIDATION_ERROR)
            .errors(List.of(VALIDATION_ERROR_V))
            .expectedApiResponse(returned(new ApiResponse<>(ApiResponseStatus.VALIDATION_ERROR, null, List.of(VALIDATION_ERROR_V))))
            .build();
    }

    private static ErrorTestCase multipleErrorsTestCase() {
        return ErrorTestCase.builder()
            .status(ApiResponseStatus.VALIDATION_ERROR)
            .errors(List.of(VALIDATION_ERROR_V, NOT_FOUND_ERROR_V))
            .expectedApiResponse(
                returned(new ApiResponse<>(ApiResponseStatus.VALIDATION_ERROR, null, List.of(VALIDATION_ERROR_V, NOT_FOUND_ERROR_V)))
            )
            .build();
    }

    private static ErrorTestCase nullErrorsTestCase() {
        return ErrorTestCase.builder()
            .status(ApiResponseStatus.FORBIDDEN)
            .errors(null)
            .expectedApiResponse(returned(new ApiResponse<>(ApiResponseStatus.FORBIDDEN, null, List.of())))
            .build();
    }
}
