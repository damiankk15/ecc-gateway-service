package com.ecc.gateway.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Immutable value object representing a single error included in an {@link ApiResponse} when a request fails. The optional {@code field} is present
 * only for field-level validation failures and is omitted from JSON when {@code null}.
 *
 * @param code short, machine-readable error type (e.g. {@code "VALIDATION_ERROR"}, {@code "NOT_FOUND"})
 * @param message human-readable description of what went wrong
 * @param field the request field this error relates to, or {@code null} if it isn't field-specific
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(String code, String message, String field) {}
