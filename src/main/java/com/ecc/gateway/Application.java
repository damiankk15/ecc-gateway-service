package com.ecc.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

/**
 * Entry point for the ECC Gateway Service. Bootstraps the Spring Boot application and declares the global OpenAPI definition (title, version,
 * contact, license) together with the HTTP Bearer JWT security scheme used across all protected endpoints.
 * 
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
@OpenAPIDefinition(
    info = @Info(
        title = "${api.title}",
        version = "${api.version}",
        description = "${api.description}",
        license = @License( name = "Apache 2.0", url = "http://www.apache.org/licenses/LICENSE-2.0.html" ),
        contact = @Contact( name = "Damian", email = "damiankk15@interia.pl" ) ) )
@SecurityScheme(
    name = "Authorization",
    scheme = "bearer",
    bearerFormat = "JWT",
    type = SecuritySchemeType.HTTP,
    in = SecuritySchemeIn.HEADER )
@SpringBootApplication
public class Application
{
    /**
     * Starts the Spring Boot application.
     * 
     * @param aArgs command-line arguments passed to the JVM
     */
    public static void main( String[] aArgs )
    {
        SpringApplication.run( Application.class, aArgs );
    }
}
