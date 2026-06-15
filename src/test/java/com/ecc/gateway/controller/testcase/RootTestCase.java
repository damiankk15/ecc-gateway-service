package com.ecc.gateway.controller.testcase;

import java.util.Map;

import org.springframework.http.HttpStatus;

import com.ecc.gateway.controller.dto.HypermediaResponse;

public class RootTestCase
{
    // input

    // mocked values
    public Map< String, String > mockedApiEndpoints;

    // expected results
    public HttpStatus expectedStatus;
    public HypermediaResponse expectedRoot;

    public static final class Builder
    {
        private final RootTestCase instance = new RootTestCase();

        public Builder withMockedApiEndpoints( Map< String, String > aMockedApiEndpoints )
        {
            instance.mockedApiEndpoints = aMockedApiEndpoints;
            return this;
        }

        public Builder withExpectedStatus( HttpStatus aExpectedStatus )
        {
            instance.expectedStatus = aExpectedStatus;
            return this;
        }

        public Builder withExpectedRoot( HypermediaResponse aExpectedRoot )
        {
            instance.expectedRoot = aExpectedRoot;
            return this;
        }

        public RootTestCase build()
        {
            return instance;
        }
    }
}
