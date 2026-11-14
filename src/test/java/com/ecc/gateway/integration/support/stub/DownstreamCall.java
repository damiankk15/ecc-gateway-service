package com.ecc.gateway.integration.support.stub;

import lombok.Builder;

@Builder
public record DownstreamCall(String service, String uri, String host, String authorization) {}
