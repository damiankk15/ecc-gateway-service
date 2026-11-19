package com.ecc.gateway.integration.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ecc.gateway.integration.security.testcase.AccessTestCase;
import com.ecc.gateway.integration.support.AbstractGatewayIntegrationTest;
import com.ecc.gateway.integration.support.stub.DownstreamStubs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.reactive.server.EntityExchangeResult;

class SecurityIntegrationTest extends AbstractGatewayIntegrationTest {

    @DisplayName("[security] -> Should let through public paths and requests with a valid token, and reject everything else")
    @ParameterizedTest(name = "[{index}] -> {argumentSetName}")
    @ArgumentsSource(AccessTestCase.ArgumentsProvider.class)
    void shouldEnforceAccessRules(AccessTestCase testCase) {
        // given

        // when
        EntityExchangeResult<String> response = exchange(HttpMethod.GET, testCase.path(), testCase.requestHeaders());

        // then
        String challenge = response.getResponseHeaders().getFirst(HttpHeaders.WWW_AUTHENTICATE);

        assertEquals(testCase.expectedStatus(), response.getStatus());
        assertEquals(testCase.expectedBearerChallenge(), challenge != null && challenge.startsWith("Bearer"));
        assertEquals(testCase.expectedDownstreamRequests(), DownstreamStubs.receivedRequests());
    }
}
