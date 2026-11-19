package com.ecc.gateway.integration.configuration.testcase;

import static org.junit.jupiter.params.provider.Arguments.argumentSet;

import java.util.List;
import java.util.stream.Stream;
import lombok.Builder;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.support.ParameterDeclarations;

@Builder
public record CorsPropertiesBindTestCase(
    // input
    List<String> propertyValues,
    // expected results
    List<String> expectedAllowedOrigins
) {
    private static final String UI_ORIGIN_V = "https://ecc.dev.local";
    private static final String ADMIN_ORIGIN_V = "https://admin.ecc.dev.local";

    public static class ArgumentsProvider implements org.junit.jupiter.params.provider.ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
            return Stream.of(
                argumentSet("No configured origins", noConfiguredOriginsTestCase()),
                argumentSet("Single origin", singleOriginTestCase()),
                argumentSet("Comma-separated origins as in the environment variable", commaSeparatedOriginsTestCase()),
                argumentSet("Comma-separated origins with spaces", commaSeparatedOriginsWithSpacesTestCase()),
                argumentSet("Indexed origins as in YAML lists", indexedOriginsTestCase())
            );
        }
    }

    private static CorsPropertiesBindTestCase noConfiguredOriginsTestCase() {
        return CorsPropertiesBindTestCase.builder().propertyValues(List.of()).expectedAllowedOrigins(List.of()).build();
    }

    private static CorsPropertiesBindTestCase singleOriginTestCase() {
        return CorsPropertiesBindTestCase.builder()
            .propertyValues(List.of("api.cors.allowed-origins=" + UI_ORIGIN_V))
            .expectedAllowedOrigins(List.of(UI_ORIGIN_V))
            .build();
    }

    private static CorsPropertiesBindTestCase commaSeparatedOriginsTestCase() {
        return CorsPropertiesBindTestCase.builder()
            .propertyValues(List.of("api.cors.allowed-origins=" + UI_ORIGIN_V + "," + ADMIN_ORIGIN_V))
            .expectedAllowedOrigins(List.of(UI_ORIGIN_V, ADMIN_ORIGIN_V))
            .build();
    }

    private static CorsPropertiesBindTestCase commaSeparatedOriginsWithSpacesTestCase() {
        return CorsPropertiesBindTestCase.builder()
            .propertyValues(List.of("api.cors.allowed-origins=" + UI_ORIGIN_V + ", " + ADMIN_ORIGIN_V))
            .expectedAllowedOrigins(List.of(UI_ORIGIN_V, ADMIN_ORIGIN_V))
            .build();
    }

    private static CorsPropertiesBindTestCase indexedOriginsTestCase() {
        return CorsPropertiesBindTestCase.builder()
            .propertyValues(List.of("api.cors.allowed-origins[0]=" + UI_ORIGIN_V, "api.cors.allowed-origins[1]=" + ADMIN_ORIGIN_V))
            .expectedAllowedOrigins(List.of(UI_ORIGIN_V, ADMIN_ORIGIN_V))
            .build();
    }
}
