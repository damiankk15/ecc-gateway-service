package com.ecc.gateway.controller.dto;

import java.util.List;

/**
 * Generic envelope returned by every Gateway API endpoint. The {@code status} field always indicates the outcome; on success {@code data} carries the
 * response payload and {@code errors} is an empty list; on failure {@code data} is {@code null} and {@code errors} contains one or more
 * {@link ApiError} entries. This contract mirrors the {@code ApiResponse<T>} type used on the frontend.
 *
 * @param <T>    type of the response payload
 * @param status outcome of the request
 * @param data   response payload, or {@code null} when {@code status} is not {@link ApiResponseStatus#OK}
 * @param errors list of errors; empty when {@code status} is {@link ApiResponseStatus#OK}
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
public record ApiResponse< T >( ApiResponseStatus status, T data, List< ApiError > errors )
{
    /**
     * Creates a successful response wrapping the given payload.
     *
     * @param <T>  type of the response payload
     * @param data the payload to wrap
     * @return an {@link ApiResponse} with status {@link ApiResponseStatus#OK}, the given data, and an empty error list
     */
    public static < T > ApiResponse< T > ok( T data )
    {
        return new ApiResponse<>( ApiResponseStatus.OK, data, List.of() );
    }

    /**
     * Creates a failed response with the given status and errors.
     *
     * @param <T>    type of the (absent) response payload
     * @param status a non-OK status describing the failure
     * @param errors one or more errors describing what went wrong
     * @return an {@link ApiResponse} with the given status, {@code null} data, and the provided errors
     */
    public static < T > ApiResponse< T > error( ApiResponseStatus status, List< ApiError > errors )
    {
        return new ApiResponse<>( status, null, errors );
    }
}
