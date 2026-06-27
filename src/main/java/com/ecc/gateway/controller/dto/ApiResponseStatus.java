package com.ecc.gateway.controller.dto;

/**
 * Enumeration of all possible response status values returned by the Gateway API. These values mirror the {@code ApiResponseStatus} enum used on the
 * frontend, ensuring a consistent contract across the stack.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
public enum ApiResponseStatus
{
    OK,
    VALIDATION_ERROR,
    NOT_FOUND_ERROR,
    BAD_CREDENTIALS,
    FORBIDDEN
}
