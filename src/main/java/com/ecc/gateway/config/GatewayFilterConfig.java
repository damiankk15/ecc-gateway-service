package com.ecc.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.headers.HttpHeadersFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

@Configuration
public class GatewayFilterConfig {

    private static final Logger log = LoggerFactory.getLogger(GatewayFilterConfig.class);

    @Bean
    public HttpHeadersFilter forwardedProtoHeadersFilter() {
        log.info(">>> Registering forwardedProtoHeadersFilter");
        return (input, exchange) -> {
            HttpHeaders headers = new HttpHeaders();
            headers.addAll(input);
            headers.set("X-Forwarded-Proto", "https");
            headers.set("X-Forwarded-Port", "443");
            String host = input.getFirst("Host");
            if (host != null) {
                headers.set("X-Forwarded-Host", host);
            }
            log.info(">>> Setting X-Forwarded-Proto=https for outgoing request");
            return headers;
        };
    }
}