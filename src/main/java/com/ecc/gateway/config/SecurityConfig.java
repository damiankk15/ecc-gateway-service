package com.ecc.gateway.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.adapter.ForwardedHeaderTransformer;

import com.ecc.gateway.config.properties.CorsProperties;

/**
 * Security configuration for the Spring WebFlux Gateway API. Disables CSRF, configures CORS from {@link CorsProperties}, enforces JWT-based OAuth2
 * resource server authentication on all exchanges except publicly accessible paths, and registers a {@link ForwardedHeaderTransformer} so that
 * controllers receive the correct public-facing URL when the gateway runs behind a reverse proxy.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig
{
    private final CorsProperties corsProperties;

    /**
     * Creates a new {@link SecurityConfig} with the provided {@link CorsProperties}.
     *
     * @param aCorsProperties CORS settings resolved from {@code application.yml}
     */
    public SecurityConfig( CorsProperties aCorsProperties )
    {
        this.corsProperties = aCorsProperties;
    }

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
     * Builds a reactive {@link CorsConfigurationSource} from the origins declared in {@link CorsProperties}. Credentials are allowed so that the
     * browser includes the {@code Authorization} header on cross-origin requests.
     *
     * @return a {@link CorsConfigurationSource} applied to all paths
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource()
    {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins( corsProperties.getAllowedOrigins() );
        config.setAllowedMethods( List.of( "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS" ) );
        config.setAllowedHeaders( List.of( "*" ) );
        config.setAllowCredentials( true );

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration( "/**", config );
        return source;
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
            .cors( cors -> cors.configurationSource( corsConfigurationSource() ) )
            .csrf( csrf -> csrf.disable() )
            .authorizeExchange( exchanges -> exchanges
                .pathMatchers( "/actuator/health", "/actuator/info" ).permitAll()
                .pathMatchers( "/swagger-ui.html", "/swagger-ui/**" ).permitAll()
                .pathMatchers( "/v3/api-docs/**" ).permitAll()
                .pathMatchers( "/jobs/v3/api-docs/**" ).permitAll()
                .anyExchange().authenticated() )
            .oauth2ResourceServer( oauth2 -> oauth2
                .jwt( Customizer.withDefaults() ) )
            .build();
    }
}
