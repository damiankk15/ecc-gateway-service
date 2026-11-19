package com.ecc.gateway.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ecc.gateway.dto.ApiResponse;
import com.ecc.gateway.exception.testcase.RouteFailureHandleTestCase;
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
class RouteFailureExceptionHandlerTest {

    @Mock
    private ApiResponseWriter apiResponseWriter;

    @Captor
    private ArgumentCaptor<ApiResponse<?>> writtenBody;

    @InjectMocks
    private RouteFailureExceptionHandler routeFailureExceptionHandler;

    @DisplayName("[handle] -> Should write not found or gateway timeout api response with the same status, or pass on other exceptions")
    @ParameterizedTest(name = "[{index}] -> {argumentSetName}")
    @ArgumentsSource(RouteFailureHandleTestCase.ArgumentsProvider.class)
    void shouldHandleRouteFailure(RouteFailureHandleTestCase testCase) {
        // given
        lenient()
            .when(apiResponseWriter.write(eq(testCase.exchange().getResponse()), any()))
            .thenReturn(Mono.empty());

        // when
        Outcome<Void> outcome = Outcome.of(() -> routeFailureExceptionHandler.handle(testCase.exchange(), testCase.exception()).block());

        // then
        assertEquals(testCase.expectedOutcome(), outcome);
        assertEquals(testCase.expectedStatus(), testCase.exchange().getResponse().getStatusCode());
        verify(apiResponseWriter, times(testCase.expectedWrittenBodies().size())).write(eq(testCase.exchange().getResponse()), writtenBody.capture());
        assertEquals(testCase.expectedWrittenBodies(), writtenBody.getAllValues());
    }
}
