package com.ecc.gateway.controller;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.ecc.gateway.config.properties.ApiResources;
import com.ecc.gateway.controller.dto.HypermediaResponse;
import com.ecc.gateway.controller.dto.HypermediaResponse.LinkObject;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Mono;

/**
 * REST controller that serves the API root ({@code GET /api}) and returns HATEOAS-style hypermedia links pointing to all resources exposed by the
 * Gateway. Clients can use these links for dynamic API discovery without hard-coding downstream URLs.
 * 
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
@Tag( name = "Gateway API", description = "Hypermedia links" )
@RestController
@RequestMapping( "/api" )
public class RootController
{
    private final ApiResources apiResources;

    /**
     * Creates a new {@link RootController} with a provided {@link ApiResources} configuration.
     * 
     * @param aApiResources component providing resources definitions to be exposed as hypermedia links
     */
    public RootController( ApiResources aApiResources )
    {
        this.apiResources = aApiResources;
    }

    /**
     * Returns a {@link HypermediaResponse} containing absolute hypermedia links for all configured API resources. The base URL (scheme, host, port)
     * is resolved from the incoming request, which already reflects the public-facing address after {@code ForwardedHeaderTransformer} processing.
     * 
     * @param aRequest the incoming HTTP request used to resolve the public base URL
     * @return a reactive {@link Mono} emitting the hypermedia link map
     */
    @Operation(
        summary = "Returns hypermedia links for API discovery",
        description = "Exposes HATEOAS-style links that allow clients to dynamically discover available API resources exposed by the Gateway.",
        responses =
        {
            @ApiResponse(
                responseCode = "200",
                description = "OK",
                content =
                {
                    @Content(
                        mediaType = "application/json",
                        schema = @Schema( implementation = HypermediaResponse.class ),
                        examples =
                        {
                            @ExampleObject(
                                name = "Example response",
                                value = """
                                    {"_links":{"jobs":{"href":"http://ecc-api.local/api/jobs"}}}""" )
                        } )
                } )
        } )
    @GetMapping
    public Mono< HypermediaResponse > root( ServerHttpRequest aRequest )
    {
        String baseUrl = resolveBaseUrl( aRequest );

        Map< String, LinkObject > links = apiResources.getResources().entrySet().stream()
            .collect( Collectors.toMap( Map.Entry::getKey, e -> new LinkObject( baseUrl + e.getValue() ) ) );

        return Mono.just( new HypermediaResponse( links ) );
    }

    /**
     * Extracts the base URL (scheme, host, and port) from the incoming request by stripping the path and query string.
     * 
     * @param aRequest the HTTP request from which the base URL is extracted
     * @return a base URL string in the form {@code scheme://host[:port]}
     */
    private String resolveBaseUrl( ServerHttpRequest aRequest )
    {
        UriComponents uriComponents = UriComponentsBuilder.fromUri( aRequest.getURI() )
            .replacePath( null )
            .replaceQuery( null )
            .build();

        return uriComponents.toString();
    }
}
