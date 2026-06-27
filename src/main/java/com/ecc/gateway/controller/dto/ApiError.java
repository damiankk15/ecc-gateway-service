package com.ecc.gateway.controller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Immutable value object representing a single API error. The optional {@code field} is present only for field-level validation failures; {@code name}
 * carries a machine-readable error code and {@code message} carries a human-readable description.
 *
 * @param field   the field that caused the error, or {@code null} when the error is not field-specific
 * @param name    machine-readable error code (e.g. {@code "REQUIRED"})
 * @param message human-readable description of the error
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
@JsonInclude( JsonInclude.Include.NON_NULL )
public record ApiError( String field, String name, String message )
{

}
