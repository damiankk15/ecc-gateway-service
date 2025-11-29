package com.ecc.gateway.controller;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.ecc.gateway.config.properties.ApiEndpoints;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Mono;

/**
 * This controller that provides hypermedia links to key actions and resources available in the API.
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
    private final ApiEndpoints apiEndpoints;

    /**
     * Creates a new {@link RootController} with a provided {@link ApiEndpoints} configuration.
     * 
     * @param aApiEndpoints component providing endpoint definitions to be exposed
     */
    public RootController( ApiEndpoints aApiEndpoints )
    {
        this.apiEndpoints = aApiEndpoints;
    }

    /**
     * Returns a map of hypermedia links for API discovery.
     * 
     * @param aRequest the incoming HTTP request used to resolve protocol, host, and port
     * @return a reactive {@link Mono} containing hypermedia links
     */
    @Operation(
        summary = "Returns hypermedia links for API discovery",
        description = "Exposes HATEOAS-style links that allow clients to dynamically discover available API operations and resources exposed by the "
            + "Gateway.",
        responses =
        {
            @ApiResponse(
                responseCode = "200",
                description = "OK",
                content =
                {
                    @Content(
                        mediaType = "application/json",
                        schema = @Schema( implementation = Map.class ),
                        examples =
                        {
                            @ExampleObject(
                                name = "Example response",
                                value = "{"
                                    + "\"_links\": {"
                                        + "\"signup\": {"
                                            + "\"href\": \"http://ecc-api.dev.local/api/auth/actions/signup\""
                                        + "}"
                                    + "}"
                                + "}" )
                        } )
                } )
        } )
    @GetMapping
    public Mono< Map< String, Object > > root( ServerHttpRequest aRequest )
    {
        String baseUrl = resolveBaseUrl( aRequest );

        Map< String, Object > links = apiEndpoints.getEndpoints().entrySet().stream()
            .collect( Collectors.toMap( Map.Entry::getKey, value -> Map.of( "href", baseUrl + value.getValue() ) ) );

        return Mono.just( Map.of( "_links", links ) );
    }

    /**
     * Resolves the base URL (scheme, host, port) from the incoming request.
     * 
     * @param aRequest the HTTP request from which the base URL is extracted
     * @return a base URL string
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
