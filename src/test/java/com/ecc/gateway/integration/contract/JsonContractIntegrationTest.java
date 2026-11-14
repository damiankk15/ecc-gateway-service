package com.ecc.gateway.integration.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ecc.gateway.integration.contract.testcase.ApiResponseSerializeTestCase;
import com.ecc.gateway.integration.contract.testcase.HypermediaResponseSerializeTestCase;
import com.ecc.gateway.integration.support.AbstractGatewayIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.json.JsonMapper;

class JsonContractIntegrationTest extends AbstractGatewayIntegrationTest {

    @Autowired
    private JsonMapper jsonMapper;

    @DisplayName("[ApiResponse] -> Should serialize response to the JSON shape expected by the frontend")
    @ParameterizedTest(name = "[{index}] -> {argumentSetName}")
    @ArgumentsSource(ApiResponseSerializeTestCase.ArgumentsProvider.class)
    void shouldSerializeApiResponse(ApiResponseSerializeTestCase testCase) {
        // given

        // when
        String json = jsonMapper.writeValueAsString(testCase.response());

        // then
        assertEquals(testCase.expectedJson(), json);
    }

    @DisplayName("[HypermediaResponse] -> Should serialize links under _links and omit templated flag when false")
    @ParameterizedTest(name = "[{index}] -> {argumentSetName}")
    @ArgumentsSource(HypermediaResponseSerializeTestCase.ArgumentsProvider.class)
    void shouldSerializeHypermediaResponse(HypermediaResponseSerializeTestCase testCase) {
        // given

        // when
        String json = jsonMapper.writeValueAsString(testCase.response());

        // then
        assertEquals(testCase.expectedJson(), json);
    }
}
