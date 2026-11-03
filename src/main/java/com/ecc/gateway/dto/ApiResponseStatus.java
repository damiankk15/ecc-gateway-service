package com.ecc.gateway.dto;

/**
 * Status values used in the {@link ApiResponse} envelope returned by the Gateway's own endpoints. The values mirror the {@code ApiResponseStatus}
 * enum used on the frontend, except {@code NETWORK_ERROR}, which the frontend sets on the client side when no response is received.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
public enum ApiResponseStatus {
    /** The request succeeded. */
    OK,
    /** The request failed validation. */
    VALIDATION_ERROR,
    /** The requested resource does not exist. */
    NOT_FOUND_ERROR,
    /** The request is not authenticated: credentials or the access token are missing or invalid. */
    BAD_CREDENTIALS,
    /** The caller is authenticated but not allowed to perform the request. */
    FORBIDDEN,
}
