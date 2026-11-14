package com.ecc.gateway.integration.support.stub;

import com.ecc.gateway.integration.support.jwt.TestJwtIssuer;
import com.ecc.gateway.integration.support.stub.StubDownstreamServer.RecordedRequest;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpHeaders;

public final class DownstreamStubs {

    public static final StubDownstreamServer JOB_SERVICE = StubDownstreamServer.start("job-service", "{\"service\":\"job-service\"}");
    public static final StubDownstreamServer COMPANY_SERVICE = StubDownstreamServer.start("company-service", "{\"service\":\"company-service\"}");
    public static final StubDownstreamServer KEYCLOAK = StubDownstreamServer.start("keycloak", TestJwtIssuer.jwkSetJson());

    private static final List<StubDownstreamServer> SERVICES = List.of(JOB_SERVICE, COMPANY_SERVICE);

    private DownstreamStubs() {}

    public static void reset() {
        SERVICES.forEach(StubDownstreamServer::reset);
    }

    public static long receivedRequests() {
        return SERVICES.stream()
            .filter(service -> service.lastRequest() != null)
            .count();
    }

    public static RecordedRequest lastRequest() {
        return SERVICES.stream().map(StubDownstreamServer::lastRequest).filter(Objects::nonNull).findFirst().orElse(null);
    }

    public static DownstreamCall lastCall() {
        List<DownstreamCall> calls = SERVICES.stream()
            .filter(service -> service.lastRequest() != null)
            .map(DownstreamStubs::toCall)
            .toList();

        if (calls.size() > 1) {
            throw new IllegalStateException("Expected at most one downstream service to be called, but got " + calls);
        }

        return calls.isEmpty() ? null : calls.getFirst();
    }

    private static DownstreamCall toCall(StubDownstreamServer service) {
        RecordedRequest request = service.lastRequest();

        return DownstreamCall.builder()
            .service(service.name())
            .uri(request.uri())
            .host(request.headers().getFirst(HttpHeaders.HOST))
            .authorization(request.headers().getFirst(HttpHeaders.AUTHORIZATION))
            .build();
    }
}
