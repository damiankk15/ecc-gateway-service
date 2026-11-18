package com.ecc.gateway.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ecc.gateway.exception.testcase.HandleTestCase;
import com.ecc.gateway.support.Outcome;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class DownstreamUnavailableExceptionHandlerTest {

    @Mock
    private JsonMapper jsonMapper;

    @InjectMocks
    private DownstreamUnavailableExceptionHandler downstreamUnavailableExceptionHandler;

    @DisplayName("[handle] -> Should write service unavailable api response for downstream connection failures, or pass on other exceptions")
    @ParameterizedTest(name = "[{index}] -> {argumentSetName}")
    @ArgumentsSource(HandleTestCase.ArgumentsProvider.class)
    void shouldHandleDownstreamConnectionFailure(HandleTestCase testCase) {
        // given
        lenient()
            .when(jsonMapper.writeValueAsBytes(any()))
            .thenReturn(new byte[0]);
        ArgumentCaptor<Object> writtenBody = ArgumentCaptor.forClass(Object.class);

        // when
        Outcome<Void> outcome = Outcome.of(() -> downstreamUnavailableExceptionHandler.handle(testCase.exchange(), testCase.exception()).block());

        // then
        assertEquals(testCase.expectedOutcome(), outcome);
        assertEquals(testCase.expectedStatus(), testCase.exchange().getResponse().getStatusCode());
        assertEquals(testCase.expectedContentType(), testCase.exchange().getResponse().getHeaders().getContentType());
        verify(jsonMapper, times(testCase.expectedWrittenBodies().size())).writeValueAsBytes(writtenBody.capture());
        assertEquals(testCase.expectedWrittenBodies(), writtenBody.getAllValues());
    }
}
