package com.ecc.gateway.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Immutable response body returned by the {@code GET /api} endpoint, representing a HATEOAS-style API index. Each entry in {@code _links} maps a
 * resource name (e.g. {@code "jobs"}) to a {@link LinkObject} holding its absolute URL, allowing clients to discover available resources
 * dynamically without hard-coding any paths.
 *
 * @param links unmodifiable links to the available resources, keyed by resource name, in the order they are configured; serialized as {@code _links}
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
public record HypermediaResponse(@JsonProperty("_links") Map<String, LinkObject> links) {
    /**
     * Stores an unmodifiable copy of {@code links} that keeps the iteration order of the given map, replacing {@code null} with an empty map.
     *
     * @param links links to the available resources, keyed by resource name, or {@code null} for none
     */
    public HypermediaResponse {
        links = links == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(links));
    }

    /**
     * Immutable value object representing a single HATEOAS link to a resource.
     *
     * @param href absolute URL of the linked resource, possibly a URI template (e.g. {@code "http://ecc-api.local/api/jobs/{jobId}"})
     * @param templated whether {@code href} is a URI template that a client must expand before use; omitted from JSON when {@code false}
     */
    public record LinkObject(String href, @JsonInclude(JsonInclude.Include.NON_DEFAULT) boolean templated) {
        /**
         * Creates a non-templated {@link LinkObject} for a plain absolute URL.
         *
         * @param href absolute URL of the linked resource
         */
        public LinkObject(String href) {
            this(href, false);
        }
    }
}
