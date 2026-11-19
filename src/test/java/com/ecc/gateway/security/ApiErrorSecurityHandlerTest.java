package com.ecc.gateway.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecc.gateway.dto.ApiResponse;
import com.ecc.gateway.security.testcase.CommenceTestCase;
import com.ecc.gateway.security.testcase.HandleTestCase;
import com.ecc.gateway.web.ApiResponseWriter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class ApiErrorSecurityHandlerTest {

    @Mock
    private ApiResponseWriter apiResponseWriter;

    @Captor
    private ArgumentCaptor<ApiResponse<?>> writtenBody;

    @InjectMocks
    private ApiErrorSecurityHandler apiErrorSecurityHandler;

    @DisplayName("[commence] -> Should keep the bearer token status and challenge and write a bad credentials api response")
    @ParameterizedTest(name = "[{index}] -> {argumentSetName}")
    @ArgumentsSource(CommenceTestCase.ArgumentsProvider.class)
    void shouldRejectUnauthenticatedRequest(CommenceTestCase testCase) {
        // given
        when(apiResponseWriter.write(eq(testCase.exchange().getResponse()), any())).thenReturn(Mono.empty());

        // when
        apiErrorSecurityHandler.commence(testCase.exchange(), testCase.exception()).block();

        // then
        assertEquals(testCase.expectedStatus(), testCase.exchange().getResponse().getStatusCode());
        assertEquals(testCase.expectedWwwAuthenticate(), testCase.exchange().getResponse().getHeaders().getFirst(HttpHeaders.WWW_AUTHENTICATE));
        verify(apiResponseWriter, times(testCase.expectedWrittenBodies().size())).write(eq(testCase.exchange().getResponse()), writtenBody.capture());
        assertEquals(testCase.expectedWrittenBodies(), writtenBody.getAllValues());
    }

    @DisplayName("[handle] -> Should keep the bearer token status and challenge and write a forbidden api response")
    @ParameterizedTest(name = "[{index}] -> {argumentSetName}")
    @ArgumentsSource(HandleTestCase.ArgumentsProvider.class)
    void shouldRejectRequestWithoutAccess(HandleTestCase testCase) {
        // given
        when(apiResponseWriter.write(eq(testCase.exchange().getResponse()), any())).thenReturn(Mono.empty());

        // when
        apiErrorSecurityHandler.handle(testCase.exchange(), testCase.exception()).block();

        // then
        assertEquals(testCase.expectedStatus(), testCase.exchange().getResponse().getStatusCode());
        assertEquals(testCase.expectedWwwAuthenticate(), testCase.exchange().getResponse().getHeaders().getFirst(HttpHeaders.WWW_AUTHENTICATE));
        verify(apiResponseWriter, times(testCase.expectedWrittenBodies().size())).write(eq(testCase.exchange().getResponse()), writtenBody.capture());
        assertEquals(testCase.expectedWrittenBodies(), writtenBody.getAllValues());
    }
}
