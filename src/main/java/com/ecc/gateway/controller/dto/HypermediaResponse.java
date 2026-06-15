package com.ecc.gateway.controller.dto;

import java.util.Map;

/**
 * Immutable response body returned by the {@code GET /api} endpoint, representing a HATEOAS-style API index. Each entry in {@code _links} maps the 
 * name of such a resource (e.g. {@code "jobs"}) to a {@link LinkObject} holding its absolute URL, allowing clients to discover available resources 
 * dynamically without hard-coding any paths.
 * 
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
public record HypermediaResponse( Map< String, LinkObject > _links )
{
    /**
     * Immutable value object representing a single HATEOAS link. Declared as a nested {@code record} so it is co-located with the response it 
     * belongs to and carries no state beyond the absolute URL it wraps.
     * 
     * @param href absolute URL of the linked resource (e.g. {@code "http://ecc-api.local/api/jobs"})
     */
    public record LinkObject( String href )
    {

    }
}
