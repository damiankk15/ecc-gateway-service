package com.ecc.gateway.config;

import com.ecc.gateway.config.properties.CorsProperties;
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

/**
 * Security configuration for the Spring WebFlux Gateway API. Disables CSRF, configures CORS from {@link CorsProperties}, and enforces JWT-based
 * OAuth2 resource server authentication on all exchanges except the publicly accessible paths.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final CorsProperties corsProperties;

    /**
     * Creates a new {@link SecurityConfig} with the provided {@link CorsProperties}.
     *
     * @param corsProperties CORS settings resolved from {@code application.yml}
     */
    public SecurityConfig(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    /**
     * Builds a reactive {@link CorsConfigurationSource} from the origins declared in {@link CorsProperties}. All request headers are allowed so that
     * the browser can send the {@code Authorization} bearer token on cross-origin requests. Credentials (cookies) are not allowed, as the API is
     * authenticated with bearer tokens only.
     *
     * @return a {@link CorsConfigurationSource} applied to all paths
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(corsProperties.getAllowedOrigins());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    /**
     * Configures the reactive Spring Security filter chain for the Gateway API. Health and info actuator endpoints, Swagger UI and the OpenAPI
     * documents of the Gateway and downstream services are publicly accessible; every other exchange requires a valid JWT bearer token issued by
     * Keycloak. CSRF protection is disabled because the API is stateless and does not use cookie-based authentication.
     *
     * @param http the {@link ServerHttpSecurity} builder used to configure WebFlux security
     * @return a configured {@link SecurityWebFilterChain} instance
     */
    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .cors(Customizer.withDefaults())
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges ->
                exchanges
                    .pathMatchers(
                        "/actuator/health",
                        "/actuator/info",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/webjars/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/jobs/v3/api-docs/**",
                        "/companies/v3/api-docs/**"
                    )
                    .permitAll()
                    .anyExchange()
                    .authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
            .build();
    }
}
