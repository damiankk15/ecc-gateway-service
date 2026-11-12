package com.ecc.gateway.dto.testcase;

import static org.junit.jupiter.params.provider.Arguments.argumentSet;

import com.ecc.gateway.dto.ApiResponse;
import com.ecc.gateway.dto.ApiResponseStatus;
import com.ecc.gateway.dto.HypermediaResponse;
import com.ecc.gateway.dto.HypermediaResponse.LinkObject;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.Builder;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.support.ParameterDeclarations;

@Builder
public record OkTestCase(
    // input
    Object data,
    // expected results
    ApiResponse<Object> expectedApiResponse
) {
    private static final String TEXT_DATA_V = "text";
    private static final LinkObject JOBS_LINK_V = new LinkObject("http://ecc-api.local/api/jobs");

    public static class ArgumentsProvider implements org.junit.jupiter.params.provider.ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
            return Stream.of(
                argumentSet("Hypermedia payload", hypermediaPayloadTestCase()),
                argumentSet("Text payload", textPayloadTestCase()),
                argumentSet("Null payload", nullPayloadTestCase())
            );
        }
    }

    private static OkTestCase hypermediaPayloadTestCase() {
        HypermediaResponse data = new HypermediaResponse(Map.of("jobs", JOBS_LINK_V));

        return OkTestCase.builder()
            .data(data)
            .expectedApiResponse(new ApiResponse<>(ApiResponseStatus.OK, data, List.of()))
            .build();
    }

    private static OkTestCase textPayloadTestCase() {
        return OkTestCase.builder()
            .data(TEXT_DATA_V)
            .expectedApiResponse(new ApiResponse<>(ApiResponseStatus.OK, TEXT_DATA_V, List.of()))
            .build();
    }

    private static OkTestCase nullPayloadTestCase() {
        return OkTestCase.builder()
            .data(null)
            .expectedApiResponse(new ApiResponse<>(ApiResponseStatus.OK, null, List.of()))
            .build();
    }
}
