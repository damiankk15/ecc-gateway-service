package com.ecc.gateway.controller.argumentsprovider;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.springframework.http.HttpStatus;

import com.ecc.gateway.controller.testcase.RootTestCase;

public class RootArgumentsProvider implements ArgumentsProvider
{
    private static final Map< String, String > API_ENDPOINTS_V = Map.of(
        "signup", "/api/auth/actions/signup" );

    @Override
    public Stream< ? extends Arguments > provideArguments( ExtensionContext aContext )
    {
        return Stream.of(
            // negative test cases
            arguments( "Empty api endpoints", emptyApiEndpointsTestCase() ),

            // positive test cases
            arguments( "Root test case", rootTestCase() ) );
    }

    private RootTestCase emptyApiEndpointsTestCase()
    {
        return new RootTestCase.Builder()
            .withMockedApiEndpoints( Collections.emptyMap() )
            .withExpectedStatus( HttpStatus.OK )
            .withExpectedRoot( Map.of( "_links", Collections.emptyMap() ) )
            .build();
    }

    private RootTestCase rootTestCase()
    {
        return new RootTestCase.Builder()
            .withMockedApiEndpoints( API_ENDPOINTS_V )
            .withExpectedStatus( HttpStatus.OK )
            .withExpectedRoot( Map.of( "_links", API_ENDPOINTS_V.entrySet().stream()
                .collect( Collectors.toMap( Map.Entry::getKey, value -> Map.of( "href", value.getValue() ) ) ) ) )
            .build();
    }
}
