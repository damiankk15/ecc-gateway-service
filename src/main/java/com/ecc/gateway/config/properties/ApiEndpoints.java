package com.ecc.gateway.config.properties;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for API endpoint definitions exposed by the Gateway.
 * 
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
@Configuration
@ConfigurationProperties( prefix = "api" )
public class ApiEndpoints
{
    private Map< String, String > endpoints = new HashMap<>();

    /**
     * Returns all configured API endpoints.
     * 
     * @return map of endpoint names to relative URI paths
     */
    public Map< String, String > getEndpoints()
    {
        return endpoints;
    }

    /**
     * Sets the map of API endpoints from external configuration.
     * 
     * @param aEndpoints mapping of endpoint names to relative URI paths
     */
    public void setEndpoints( Map< String, String > aEndpoints )
    {
        this.endpoints = aEndpoints;
    }
}
