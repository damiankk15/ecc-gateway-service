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

import com.ecc.gateway.controller.RootControllerTest;
import com.ecc.gateway.controller.dto.HypermediaResponse;
import com.ecc.gateway.controller.dto.HypermediaResponse.LinkObject;
import com.ecc.gateway.controller.testcase.RootTestCase;

public class RootArgumentsProvider implements ArgumentsProvider
{
    private static final Map< String, String > API_RESOURCES_V = Map.of(
        "jobs", "/api/jobs" );

    @Override
    public Stream< ? extends Arguments > provideArguments( ExtensionContext aContext )
    {
        return Stream.of(
            // negative test cases
            arguments( "Empty api resources", emptyApiResourcesTestCase() ),

            // positive test cases
            arguments( "Root test case", rootTestCase() ) );
    }

    private RootTestCase emptyApiResourcesTestCase()
    {
        return new RootTestCase.Builder()
            .withMockedApiResources( Collections.emptyMap() )
            .withExpectedStatus( HttpStatus.OK )
            .withExpectedRoot( new HypermediaResponse( Collections.emptyMap() ) )
            .build();
    }

    private RootTestCase rootTestCase()
    {
        return new RootTestCase.Builder()
            .withMockedApiResources( API_RESOURCES_V )
            .withExpectedStatus( HttpStatus.OK )
            .withExpectedRoot( new HypermediaResponse( API_RESOURCES_V.entrySet().stream()
                .collect( Collectors.toMap( Map.Entry::getKey, e -> new LinkObject( RootControllerTest.BASE_URL + e.getValue() ) ) ) ) )
            .build();
    }
}
