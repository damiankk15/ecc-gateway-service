package com.ecc.gateway.controller;

import com.ecc.gateway.config.properties.ApiResources;
import com.ecc.gateway.dto.ApiResponse;
import com.ecc.gateway.dto.HypermediaResponse;
import com.ecc.gateway.dto.HypermediaResponse.LinkObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;
import reactor.core.publisher.Mono;

/**
 * REST controller that serves the API root ({@code GET /api}) and returns HATEOAS-style hypermedia links pointing to all resources exposed by the
 * Gateway. Clients can use these links for dynamic API discovery without hard-coding resource URLs.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
@Tag(name = "Gateway API", description = "Hypermedia links")
@RestController
@RequestMapping("/api")
public class RootController {

    private final ApiResources apiResources;

    /**
     * Creates a new {@link RootController} with the provided {@link ApiResources} configuration.
     *
     * @param apiResources component providing resource definitions to be exposed as hypermedia links
     */
    public RootController(ApiResources apiResources) {
        this.apiResources = apiResources;
    }

    /**
     * Returns an {@link ApiResponse} wrapping a {@link HypermediaResponse} that contains absolute hypermedia links for all configured API resources.
     * Links are returned in the order the resources are configured, and paths containing URI template variables (e.g. {@code {jobId}}) are marked as
     * {@code templated}. The base URL (scheme, host, port) is resolved from the incoming request, which reflects the public-facing address when
     * forwarded headers are applied ({@code server.forward-headers-strategy=framework}).
     *
     * @param request the incoming HTTP request used to resolve the public base URL
     * @return a reactive {@link Mono} emitting the {@link ApiResponse} envelope containing the hypermedia link map
     */
    @Operation(
        summary = "Returns hypermedia links for API discovery",
        description = "Exposes HATEOAS-style links that allow clients to dynamically discover available API resources exposed by the Gateway.",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "OK",
                content = {
                    @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ApiResponse.class),
                        examples = {
                            @ExampleObject(
                                name = "Example response",
                                value = """
                                {
                                  "status": "OK",
                                  "data": {
                                    "_links": {
                                      "jobs": { "href": "https://ecc-api.local/api/jobs" },
                                      "job": { "href": "https://ecc-api.local/api/jobs/{jobId}", "templated": true },
                                      "companies": { "href": "https://ecc-api.local/api/companies" },
                                      "markets": { "href": "https://ecc-api.local/api/markets" }
                                    }
                                  },
                                  "errors": []
                                }
                                """
                            ),
                        }
                    ),
                }
            ),
        }
    )
    @GetMapping
    public Mono<ApiResponse<HypermediaResponse>> root(ServerHttpRequest request) {
        String baseUrl = resolveBaseUrl(request);

        Map<String, LinkObject> links = apiResources
            .getResources()
            .entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> toLink(baseUrl, e.getValue()), (a, b) -> a, LinkedHashMap::new));

        return Mono.just(ApiResponse.ok(new HypermediaResponse(links)));
    }

    /**
     * Creates a {@link LinkObject} with an absolute URL for the given resource path. The link is marked as {@code templated} when the path contains
     * at least one URI template variable.
     *
     * @param baseUrl the public base URL in the form {@code scheme://host[:port]}
     * @param path the relative resource path, possibly a URI template (e.g. {@code /api/jobs/{jobId}})
     * @return a {@link LinkObject} pointing to the resource
     */
    private LinkObject toLink(String baseUrl, String path) {
        boolean templated = !new UriTemplate(path).getVariableNames().isEmpty();

        return new LinkObject(baseUrl + path, templated);
    }

    /**
     * Extracts the base URL (scheme, host, and port) from the incoming request by stripping the path and query string.
     *
     * @param request the HTTP request from which the base URL is extracted
     * @return a base URL string in the form {@code scheme://host[:port]}
     */
    private String resolveBaseUrl(ServerHttpRequest request) {
        UriComponents uriComponents = UriComponentsBuilder.fromUri(request.getURI()).replacePath(null).replaceQuery(null).build();

        return uriComponents.toString();
    }
}
