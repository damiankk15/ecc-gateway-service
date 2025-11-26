package com.ecc.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Configuration class for setting up security in a Spring WebFlux Gateway API.
 * 
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig
{
    /**
     * Configures the reactive Spring Security filter chain for the Gateway API.
     * 
     * @param aHttp the {@link ServerHttpSecurity} builder used to configure WebFlux security
     * @return a configured {@link SecurityWebFilterChain} instance
     */
    @Bean
    SecurityWebFilterChain securityWebFilterChain( ServerHttpSecurity aHttp )
    {
        return aHttp
            .csrf( csrf -> csrf.disable() )
            .authorizeExchange( exchanges -> exchanges
                .pathMatchers( "/swagger-ui/**" ).permitAll()
                .pathMatchers( "/v3/api-docs/**" ).permitAll()
                .pathMatchers( "/api" ).permitAll()
                .anyExchange().permitAll() )
            .oauth2ResourceServer( oauth2 -> oauth2
                .jwt( Customizer.withDefaults() ) )
            .build();
    }
}
