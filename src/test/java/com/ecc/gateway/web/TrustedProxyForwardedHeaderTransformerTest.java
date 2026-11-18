package com.ecc.gateway.web;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ecc.gateway.web.testcase.ApplyTestCase;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.http.server.reactive.ServerHttpRequest;

class TrustedProxyForwardedHeaderTransformerTest {

    @DisplayName("[apply] -> Should apply forwarded headers from trusted proxies, or only remove them for any other address")
    @ParameterizedTest(name = "[{index}] -> {argumentSetName}")
    @ArgumentsSource(ApplyTestCase.ArgumentsProvider.class)
    void shouldApplyForwardedHeadersOnlyFromTrustedProxies(ApplyTestCase testCase) {
        // given
        TrustedProxyForwardedHeaderTransformer transformer = new TrustedProxyForwardedHeaderTransformer(testCase.mockedTrustedProxies());

        // when
        ServerHttpRequest request = transformer.apply(testCase.request());

        // then
        assertEquals(testCase.expectedUri(), request.getURI());
        assertEquals(testCase.expectedHeaderNames(), List.copyOf(request.getHeaders().headerNames()));
    }
}
