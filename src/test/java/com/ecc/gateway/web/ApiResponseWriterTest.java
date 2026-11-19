package com.ecc.gateway.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.ecc.gateway.web.testcase.WriteTestCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class ApiResponseWriterTest {

    @Mock
    private JsonMapper jsonMapper;

    @InjectMocks
    private ApiResponseWriter apiResponseWriter;

    @DisplayName("[write] -> Should write the serialized api response as a JSON body")
    @ParameterizedTest(name = "[{index}] -> {argumentSetName}")
    @ArgumentsSource(WriteTestCase.ArgumentsProvider.class)
    void shouldWriteApiResponseAsJson(WriteTestCase testCase) {
        // given
        when(jsonMapper.writeValueAsBytes(testCase.body())).thenReturn(testCase.mockedBytes());

        // when
        apiResponseWriter.write(testCase.response(), testCase.body()).block();

        // then
        assertEquals(testCase.expectedContentType(), testCase.response().getHeaders().getContentType());
        assertEquals(testCase.expectedBody(), testCase.response().getBodyAsString().block());
    }
}
