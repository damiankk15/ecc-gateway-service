package com.ecc.gateway.integration.forwarding;

import static com.ecc.gateway.integration.support.TestHeaders.X_FORWARDED_HOST_F;
import static com.ecc.gateway.integration.support.TestHeaders.X_FORWARDED_PROTO_F;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.ecc.gateway.integration.forwarding.testcase.ForwardedHeadersTestCase;
import com.ecc.gateway.integration.support.AbstractGatewayIntegrationTest;
import com.ecc.gateway.integration.support.stub.DownstreamStubs;
import com.ecc.gateway.integration.support.stub.StubDownstreamServer.RecordedRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.reactive.server.EntityExchangeResult;

class ForwardedHeadersIntegrationTest extends AbstractGatewayIntegrationTest {

    @DisplayName("[forwarded headers] -> Should pass the public scheme and host to downstream services for requests from a trusted proxy")
    @ParameterizedTest(name = "[{index}] -> {argumentSetName}")
    @ArgumentsSource(ForwardedHeadersTestCase.ArgumentsProvider.class)
    void shouldPassForwardedHeadersToDownstreamService(ForwardedHeadersTestCase testCase) {
        // given

        // when
        EntityExchangeResult<String> response = exchange(HttpMethod.GET, testCase.path(), testCase.requestHeaders());

        // then
        RecordedRequest downstreamRequest = DownstreamStubs.lastRequest();

        assertEquals(testCase.expectedStatus(), response.getStatus());
        assertNotNull(downstreamRequest, "Expected request to reach a downstream service");
        assertEquals(testCase.expectedForwardedProto(), downstreamRequest.headers().getFirst(X_FORWARDED_PROTO_F));
        assertEquals(testCase.expectedForwardedHost(), downstreamRequest.headers().getFirst(X_FORWARDED_HOST_F));
    }
}
