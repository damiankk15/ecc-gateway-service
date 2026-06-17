package com.ecc.gateway.config.properties;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for API resource definitions exposed by the Gateway. Bound to the {@code api.resources} prefix in {@code application.yml},
 * where each key is a logical resource name and each value is its relative URI path.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
@Component
@ConfigurationProperties( prefix = "api" )
public class ApiResources
{
    private Map< String, String > resources = new HashMap<>();

    /**
     * Returns all configured API resource paths.
     *
     * @return map of logical resource names to their relative URI paths
     */
    public Map< String, String > getResources()
    {
        return resources;
    }

    /**
     * Sets the map of API resource paths from external configuration.
     *
     * @param aResources mapping of logical resource names to relative URI paths
     */
    public void setResources( Map< String, String > aResources )
    {
        this.resources = aResources;
    }
}
