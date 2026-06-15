package com.ecc.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.server.adapter.ForwardedHeaderTransformer;

/**
 * Security configuration for the Spring WebFlux Gateway API. Disables CSRF, enforces JWT-based OAuth2 resource server authentication on all 
 * exchanges except publicly accessible paths, and registers a {@link ForwardedHeaderTransformer} so that controllers receive the correct 
 * public-facing URL when the gateway runs behind a reverse proxy.
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
     * Registers a {@link ForwardedHeaderTransformer} that rewrites the request URI from {@code X-Forwarded-Host}, {@code X-Forwarded-Proto}, and 
     * {@code X-Forwarded-Port} headers before the request reaches any controller. This ensures URL generation (e.g. hypermedia links) reflects the 
     * public-facing address rather than the internal one.
     * 
     * @return a {@link ForwardedHeaderTransformer} instance
     */
    @Bean
    ForwardedHeaderTransformer forwardedHeaderTransformer()
    {
        return new ForwardedHeaderTransformer();
    }

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
                .pathMatchers( "/actuator/health", "/actuator/info" ).permitAll()
                .pathMatchers( "/swagger-ui.html", "/swagger-ui/**" ).permitAll()
                .pathMatchers( "/v3/api-docs/**" ).permitAll()
                .pathMatchers( "/auth/v3/api-docs/**" ).permitAll()
                .anyExchange().authenticated() )
            .oauth2ResourceServer( oauth2 -> oauth2
                .jwt( Customizer.withDefaults() ) )
            .build();
    }
}
