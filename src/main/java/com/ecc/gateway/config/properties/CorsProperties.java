package com.ecc.gateway.config.properties;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for CORS settings applied by the Gateway. Bound to the {@code api.cors} prefix in {@code application.yml}. Each entry in
 * {@code allowed-origins} must be a fully qualified origin (scheme + host + optional port) that the browser should be permitted to call the API from.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
@Component
@ConfigurationProperties( prefix = "api.cors" )
public class CorsProperties
{
    private List< String > allowedOrigins = new ArrayList<>();

    /**
     * Returns the list of allowed origins configured for CORS.
     *
     * @return list of fully qualified allowed origins (e.g. {@code https://ecc.dev.local})
     */
    public List< String > getAllowedOrigins()
    {
        return allowedOrigins;
    }

    /**
     * Sets the list of allowed origins from external configuration.
     *
     * @param aAllowedOrigins list of fully qualified allowed origins
     */
    public void setAllowedOrigins( List< String > aAllowedOrigins )
    {
        this.allowedOrigins = aAllowedOrigins;
    }
}
