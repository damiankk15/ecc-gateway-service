package com.ecc.gateway.integration.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ecc.gateway.config.properties.ApiResources;
import com.ecc.gateway.config.properties.CorsProperties;
import com.ecc.gateway.integration.configuration.testcase.ApiResourcesBindTestCase;
import com.ecc.gateway.integration.configuration.testcase.CorsPropertiesBindTestCase;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class PropertiesBindingIntegrationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner().withUserConfiguration(PropertiesConfiguration.class);

    @DisplayName("[api.resources] -> Should bind configured api resources in their declared order")
    @ParameterizedTest(name = "[{index}] -> {argumentSetName}")
    @ArgumentsSource(ApiResourcesBindTestCase.ArgumentsProvider.class)
    void shouldBindResourcesInDeclaredOrder(ApiResourcesBindTestCase testCase) {
        // given
        ApplicationContextRunner runner = contextRunner.withPropertyValues(testCase.propertyValues().toArray(String[]::new));

        // when
        runner.run(context -> {
            ApiResources apiResources = context.getBean(ApiResources.class);

            // then
            assertEquals(List.copyOf(testCase.expectedResources().entrySet()), List.copyOf(apiResources.getResources().entrySet()));
        });
    }

    @DisplayName("[api.cors.allowed-origins] -> Should bind allowed origins from every supported property format")
    @ParameterizedTest(name = "[{index}] -> {argumentSetName}")
    @ArgumentsSource(CorsPropertiesBindTestCase.ArgumentsProvider.class)
    void shouldBindAllowedOrigins(CorsPropertiesBindTestCase testCase) {
        // given
        ApplicationContextRunner runner = contextRunner.withPropertyValues(testCase.propertyValues().toArray(String[]::new));

        // when
        runner.run(context -> {
            CorsProperties corsProperties = context.getBean(CorsProperties.class);

            // then
            assertEquals(testCase.expectedAllowedOrigins(), corsProperties.getAllowedOrigins());
        });
    }

    @EnableConfigurationProperties({ ApiResources.class, CorsProperties.class })
    static class PropertiesConfiguration {}
}
