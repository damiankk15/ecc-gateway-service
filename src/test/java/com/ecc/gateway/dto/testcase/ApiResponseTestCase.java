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
public record ApiResponseTestCase(
    // input
    ApiResponseStatus status,
    Object data,
    List<ApiError> errors,
    // expected results
    Outcome<ApiResponse<Object>> expectedApiResponse
) {
    private static final ApiError NOT_FOUND_ERROR_V = new ApiError("NOT_FOUND", "Job 42 does not exist", null);
    private static final ApiError VALIDATION_ERROR_V = new ApiError("VALIDATION_ERROR", "must not be blank", "name");
    private static final String TEXT_DATA_V = "text";

    public static class ArgumentsProvider implements org.junit.jupiter.params.provider.ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
            return Stream.of(
                // negative test cases
                argumentSet("Only null error", onlyNullErrorTestCase()),
                argumentSet("Null error among valid errors", nullErrorAmongValidErrorsTestCase()),

                // positive test cases
                argumentSet("Null errors", nullErrorsTestCase()),
                argumentSet("Empty errors", emptyErrorsTestCase()),
                argumentSet("Single error", singleErrorTestCase()),
                argumentSet("Multiple errors in given order", multipleErrorsTestCase())
            );
        }
    }

    private static ApiResponseTestCase onlyNullErrorTestCase() {
        return ApiResponseTestCase.builder()
            .status(ApiResponseStatus.NOT_FOUND_ERROR)
            .errors(Arrays.asList((ApiError) null))
            .expectedApiResponse(thrown(NullPointerException.class))
            .build();
    }

    private static ApiResponseTestCase nullErrorAmongValidErrorsTestCase() {
        return ApiResponseTestCase.builder()
            .status(ApiResponseStatus.NOT_FOUND_ERROR)
            .errors(Arrays.asList(NOT_FOUND_ERROR_V, null))
            .expectedApiResponse(thrown(NullPointerException.class))
            .build();
    }

    private static ApiResponseTestCase nullErrorsTestCase() {
        return ApiResponseTestCase.builder()
            .status(ApiResponseStatus.OK)
            .data(TEXT_DATA_V)
            .errors(null)
            .expectedApiResponse(returned(new ApiResponse<>(ApiResponseStatus.OK, TEXT_DATA_V, List.of())))
            .build();
    }

    private static ApiResponseTestCase emptyErrorsTestCase() {
        return ApiResponseTestCase.builder()
            .status(ApiResponseStatus.OK)
            .data(TEXT_DATA_V)
            .errors(List.of())
            .expectedApiResponse(returned(new ApiResponse<>(ApiResponseStatus.OK, TEXT_DATA_V, List.of())))
            .build();
    }

    private static ApiResponseTestCase singleErrorTestCase() {
        return ApiResponseTestCase.builder()
            .status(ApiResponseStatus.NOT_FOUND_ERROR)
            .errors(List.of(NOT_FOUND_ERROR_V))
            .expectedApiResponse(returned(new ApiResponse<>(ApiResponseStatus.NOT_FOUND_ERROR, null, List.of(NOT_FOUND_ERROR_V))))
            .build();
    }

    private static ApiResponseTestCase multipleErrorsTestCase() {
        return ApiResponseTestCase.builder()
            .status(ApiResponseStatus.VALIDATION_ERROR)
            .errors(List.of(VALIDATION_ERROR_V, NOT_FOUND_ERROR_V))
            .expectedApiResponse(
                returned(new ApiResponse<>(ApiResponseStatus.VALIDATION_ERROR, null, List.of(VALIDATION_ERROR_V, NOT_FOUND_ERROR_V)))
            )
            .build();
    }
}
