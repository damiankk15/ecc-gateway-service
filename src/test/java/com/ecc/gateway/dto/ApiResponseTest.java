package com.ecc.gateway.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ecc.gateway.dto.testcase.ApiResponseTestCase;
import com.ecc.gateway.dto.testcase.ErrorTestCase;
import com.ecc.gateway.dto.testcase.OkTestCase;
import com.ecc.gateway.support.Outcome;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

class ApiResponseTest {

    @DisplayName("[ApiResponse] -> Should create api response with given status and data and errors normalized to a list, or reject null errors")
    @ParameterizedTest(name = "[{index}] -> {argumentSetName}")
    @ArgumentsSource(ApiResponseTestCase.ArgumentsProvider.class)
    void shouldCreateResponse(ApiResponseTestCase testCase) {
        // given

        // when
        Outcome<ApiResponse<Object>> apiResponse = Outcome.of(() -> new ApiResponse<>(testCase.status(), testCase.data(), testCase.errors()));

        // then
        assertEquals(testCase.expectedApiResponse(), apiResponse);
    }

    @DisplayName("[ok] -> Should return successful api response with given payload and no errors")
    @ParameterizedTest(name = "[{index}] -> {argumentSetName}")
    @ArgumentsSource(OkTestCase.ArgumentsProvider.class)
    void shouldReturnSuccessfulResponse(OkTestCase testCase) {
        // given

        // when
        ApiResponse<Object> apiResponse = ApiResponse.ok(testCase.data());

        // then
        assertEquals(testCase.expectedApiResponse(), apiResponse);
    }

    @DisplayName("[error] -> Should return failed api response with given status and errors, or reject null errors")
    @ParameterizedTest(name = "[{index}] -> {argumentSetName}")
    @ArgumentsSource(ErrorTestCase.ArgumentsProvider.class)
    void shouldReturnFailedResponse(ErrorTestCase testCase) {
        // given

        // when
        Outcome<ApiResponse<Object>> apiResponse = Outcome.of(() -> ApiResponse.error(testCase.status(), testCase.errors()));

        // then
        assertEquals(testCase.expectedApiResponse(), apiResponse);
    }
}
