package com.ecc.gateway.web;

import com.ecc.gateway.dto.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import tools.jackson.databind.json.JsonMapper;

/**
 * Writes an {@link ApiResponse} as the JSON body of a response produced outside a controller, such as an error raised while routing a request or
 * rejected by Spring Security. Every such response uses the same envelope and the application's {@link JsonMapper}, so it has the same shape as
 * the responses of the Gateway's own endpoints.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
@Component
public class ApiResponseWriter {

    private final JsonMapper jsonMapper;

    /**
     * Creates a new {@link ApiResponseWriter} that serializes response bodies with the application's {@link JsonMapper}.
     *
     * @param jsonMapper mapper used to serialize the {@link ApiResponse} body
     */
    public ApiResponseWriter(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    /**
     * Sets the {@code application/json} content type and writes {@code body} as the response body. The status code is left to the caller.
     *
     * @param response the response to write to
     * @param body the envelope to serialize
     * @return a {@link Mono} completing when the body is written
     */
    public Mono<Void> write(ServerHttpResponse response, ApiResponse<?> body) {
        byte[] bytes = jsonMapper.writeValueAsBytes(body);

        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }
}
