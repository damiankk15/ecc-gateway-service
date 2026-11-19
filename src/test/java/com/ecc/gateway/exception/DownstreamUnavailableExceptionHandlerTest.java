package com.ecc.gateway.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ecc.gateway.dto.ApiResponse;
import com.ecc.gateway.exception.testcase.DownstreamUnavailableHandleTestCase;
import com.ecc.gateway.support.Outcome;
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
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class DownstreamUnavailableExceptionHandlerTest {

    @Mock
    private ApiResponseWriter apiResponseWriter;

    @Captor
    private ArgumentCaptor<ApiResponse<?>> writtenBody;

    @InjectMocks
    private DownstreamUnavailableExceptionHandler downstreamUnavailableExceptionHandler;

    @DisplayName("[handle] -> Should write service unavailable api response for downstream connection failures, or pass on other exceptions")
    @ParameterizedTest(name = "[{index}] -> {argumentSetName}")
    @ArgumentsSource(DownstreamUnavailableHandleTestCase.ArgumentsProvider.class)
    void shouldHandleDownstreamConnectionFailure(DownstreamUnavailableHandleTestCase testCase) {
        // given
        lenient()
            .when(apiResponseWriter.write(eq(testCase.exchange().getResponse()), any()))
            .thenReturn(Mono.empty());

        // when
        Outcome<Void> outcome = Outcome.of(() -> downstreamUnavailableExceptionHandler.handle(testCase.exchange(), testCase.exception()).block());

        // then
        assertEquals(testCase.expectedOutcome(), outcome);
        assertEquals(testCase.expectedStatus(), testCase.exchange().getResponse().getStatusCode());
        verify(apiResponseWriter, times(testCase.expectedWrittenBodies().size())).write(eq(testCase.exchange().getResponse()), writtenBody.capture());
        assertEquals(testCase.expectedWrittenBodies(), writtenBody.getAllValues());
    }
}
