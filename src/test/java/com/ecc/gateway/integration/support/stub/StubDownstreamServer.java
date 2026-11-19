package com.ecc.gateway.integration.support.stub;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

public final class StubDownstreamServer {

    private final String name;
    private final DisposableServer server;
    private final AtomicReference<RecordedRequest> lastRequest = new AtomicReference<>();

    private StubDownstreamServer(String name, String responseBody, Duration responseDelay) {
        this.name = name;
        this.server = HttpServer.create()
            .host("127.0.0.1")
            .port(0)
            .handle((request, response) -> {
                HttpHeaders headers = new HttpHeaders();
                request.requestHeaders().forEach(header -> headers.add(header.getKey(), header.getValue()));
                lastRequest.set(new RecordedRequest(request.method().name(), request.uri(), headers));

                return response.header(HttpHeaders.CONTENT_TYPE, "application/json").sendString(Mono.just(responseBody).delayElement(responseDelay));
            })
            .bindNow();
    }

    public static StubDownstreamServer start(String name, String responseBody) {
        return start(name, responseBody, Duration.ZERO);
    }

    public static StubDownstreamServer start(String name, String responseBody, Duration responseDelay) {
        return new StubDownstreamServer(name, responseBody, responseDelay);
    }

    public String name() {
        return name;
    }

    public String hostAndPort() {
        return "127.0.0.1:" + server.port();
    }

    public String baseUrl() {
        return "http://" + hostAndPort();
    }

    public RecordedRequest lastRequest() {
        return lastRequest.get();
    }

    public void reset() {
        lastRequest.set(null);
    }

    public record RecordedRequest(String method, String uri, HttpHeaders headers) {}
}
