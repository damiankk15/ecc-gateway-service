package com.ecc.gateway.config.properties;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for API endpoint definitions exposed by the Gateway. Bound to the {@code api.endpoints} prefix in {@code application.yml},
 * where each key is a logical endpoint name and each value is its relative URI path.
 * 
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
@Component
@ConfigurationProperties( prefix = "api" )
public class ApiEndpoints
{
    private Map< String, String > endpoints = new HashMap<>();

    /**
     * Returns all configured API endpoints.
     * 
     * @return map of logical endpoint names to their relative URI paths
     */
    public Map< String, String > getEndpoints()
    {
        return endpoints;
    }

    /**
     * Sets the map of API endpoints from external configuration.
     * 
     * @param aEndpoints mapping of logical endpoint names to relative URI paths
     */
    public void setEndpoints( Map< String, String > aEndpoints )
    {
        this.endpoints = aEndpoints;
    }
}
