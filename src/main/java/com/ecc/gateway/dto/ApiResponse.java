package com.ecc.gateway.dto;

import java.util.List;

/**
 * Generic envelope returned by the Gateway's own endpoints. The {@code status} field indicates the outcome. A successful response carries the payload
 * in {@code data} and an empty {@code errors} list; a failed response has {@code null} {@code data} and one or more {@link ApiError} entries in
 * {@code errors}. Use {@link #ok(Object)} and {@link #error(ApiResponseStatus, List)} to create instances that follow this contract. It mirrors
 * the {@code ApiResponse<T>} type used on the frontend.
 *
 * @param <T> type of the response payload
 * @param status outcome of the request
 * @param data response payload, or {@code null} when {@code status} is not {@link ApiResponseStatus#OK}
 * @param errors unmodifiable list of errors, never {@code null}; empty when {@code status} is {@link ApiResponseStatus#OK}
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
public record ApiResponse<T>(ApiResponseStatus status, T data, List<ApiError> errors) {
    /**
     * Normalizes {@code errors} to an unmodifiable copy, replacing {@code null} with an empty list, so the response always serializes an array and
     * cannot be changed through the caller's list.
     *
     * @param status outcome of the request
     * @param data response payload, or {@code null} when {@code status} is not {@link ApiResponseStatus#OK}
     * @param errors list of errors, or {@code null} for none
     * @throws NullPointerException if {@code errors} contains a {@code null} element
     */
    public ApiResponse {
        errors = errors == null ? List.of() : List.copyOf(errors);
    }

    /**
     * Creates a successful response wrapping the given payload.
     *
     * @param <T> type of the response payload
     * @param data the payload to wrap
     * @return an {@link ApiResponse} with status {@link ApiResponseStatus#OK}, the given data, and an empty error list
     */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(ApiResponseStatus.OK, data, List.of());
    }

    /**
     * Creates a failed response with the given status and errors. The arguments are not validated; the caller must pass a non-OK status and at
     * least one error.
     *
     * @param <T> type of the (absent) response payload
     * @param status a non-OK status describing the failure
     * @param errors one or more errors describing what went wrong
     * @return an {@link ApiResponse} with the given status, {@code null} data, and the provided errors
     */
    public static <T> ApiResponse<T> error(ApiResponseStatus status, List<ApiError> errors) {
        return new ApiResponse<>(status, null, errors);
    }
}
