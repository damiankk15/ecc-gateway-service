package com.ecc.gateway.web;

import java.net.InetSocketAddress;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.adapter.ForwardedHeaderTransformer;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;

/**
 * {@link ForwardedHeaderTransformer} that applies {@code Forwarded} and {@code X-Forwarded-*} headers only to requests coming from a trusted proxy.
 * Requests from any other address have these headers removed without being applied, so a client cannot spoof the public scheme, host or port by
 * sending them itself. Trusted proxies are matched by the same regular expression as the Gateway's
 * {@code spring.cloud.gateway.server.webflux.trusted-proxies} property. Registered under the bean name WebFlux looks up for its forwarded header
 * transformer, replacing Spring Boot's {@code server.forward-headers-strategy=framework}, which trusts forwarded headers from any client.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
@Component(WebHttpHandlerBuilder.FORWARDED_HEADER_TRANSFORMER_BEAN_NAME)
public class TrustedProxyForwardedHeaderTransformer extends ForwardedHeaderTransformer {

    private final Pattern trustedProxies;
    private final ForwardedHeaderTransformer removeOnlyTransformer;

    /**
     * Creates a new {@link TrustedProxyForwardedHeaderTransformer} trusting the remote addresses that fully match {@code trustedProxies}.
     *
     * @param trustedProxies regular expression matched against the remote host address (e.g. {@code 10\..*})
     */
    public TrustedProxyForwardedHeaderTransformer(@Value("${spring.cloud.gateway.server.webflux.trusted-proxies}") String trustedProxies) {
        this.trustedProxies = Pattern.compile(trustedProxies);
        this.removeOnlyTransformer = new ForwardedHeaderTransformer();
        this.removeOnlyTransformer.setRemoveOnly(true);
    }

    /**
     * Applies the forwarded headers when the request comes from a trusted proxy, otherwise removes them and leaves the request URI unchanged.
     *
     * @param request the incoming request
     * @return the request with forwarded headers applied and removed, or only removed
     */
    @Override
    public ServerHttpRequest apply(ServerHttpRequest request) {
        if (isFromTrustedProxy(request)) {
            return super.apply(request);
        }

        return removeOnlyTransformer.apply(request);
    }

    /**
     * Checks whether {@code request} comes directly from a trusted proxy, i.e. its remote host address fully matches the trusted proxies regular
     * expression. A request without a known remote address is never trusted.
     *
     * @param request the incoming request
     * @return {@code true} if the remote address is a trusted proxy, {@code false} otherwise
     */
    private boolean isFromTrustedProxy(ServerHttpRequest request) {
        InetSocketAddress remoteAddress = request.getRemoteAddress();

        return remoteAddress != null && trustedProxies.matcher(remoteAddress.getHostString()).matches();
    }
}
