package com.ecc.gateway.integration.forwarding;

import static com.ecc.gateway.integration.support.TestHeaders.X_FORWARDED_PROTO_F;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.ecc.gateway.integration.forwarding.testcase.UntrustedProxyTestCase;
import com.ecc.gateway.integration.support.AbstractGatewayIntegrationTest;
import com.ecc.gateway.integration.support.stub.DownstreamStubs;
import com.ecc.gateway.integration.support.stub.StubDownstreamServer.RecordedRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.EntityExchangeResult;

@TestPropertySource(properties = "spring.cloud.gateway.server.webflux.trusted-proxies=10\\..*")
class UntrustedProxyIntegrationTest extends AbstractGatewayIntegrationTest {

    @DisplayName("[forwarded headers] -> Should not pass spoofed forwarded headers from an untrusted address to downstream services")
    @ParameterizedTest(name = "[{index}] -> {argumentSetName}")
    @ArgumentsSource(UntrustedProxyTestCase.ArgumentsProvider.class)
    void shouldNotPassSpoofedForwardedHeaders(UntrustedProxyTestCase testCase) {
        // given

        // when
        EntityExchangeResult<String> response = exchange(HttpMethod.GET, testCase.path(), testCase.requestHeaders());

        // then
        RecordedRequest downstreamRequest = DownstreamStubs.lastRequest();

        assertEquals(testCase.expectedStatus(), response.getStatus());
        assertNotNull(downstreamRequest, "Expected request to reach a downstream service");
        assertEquals(testCase.expectedForwardedProto(), downstreamRequest.headers().getFirst(X_FORWARDED_PROTO_F));
    }
}
