package com.ecc.gateway.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ecc.gateway.dto.testcase.HypermediaResponseTestCase;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

class HypermediaResponseTest {

    @DisplayName("[HypermediaResponse] -> Should keep given links in their original order and replace null with no links")
    @ParameterizedTest(name = "[{index}] -> {argumentSetName}")
    @ArgumentsSource(HypermediaResponseTestCase.ArgumentsProvider.class)
    void shouldKeepLinksInOriginalOrder(HypermediaResponseTestCase testCase) {
        // given

        // when
        HypermediaResponse hypermediaResponse = new HypermediaResponse(testCase.links());

        // then
        assertEquals(List.copyOf(testCase.expectedHypermediaResponse().links().entrySet()), List.copyOf(hypermediaResponse.links().entrySet()));
    }
}
