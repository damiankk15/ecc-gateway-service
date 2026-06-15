package com.ecc.gateway.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.ecc.gateway.config.properties.ApiEndpoints;
import com.ecc.gateway.controller.argumentsprovider.RootArgumentsProvider;
import com.ecc.gateway.controller.dto.HypermediaResponse;
import com.ecc.gateway.controller.testcase.RootTestCase;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith( MockitoExtension.class )
public class RootControllerTest
{
    public static final String BASE_URL = "http://ecc-api.local";

    private WebTestClient webTestClient;
    private JacksonTester< HypermediaResponse > jsonTester;

    @Mock
    private ApiEndpoints apiEndpoints;

    @InjectMocks
    private RootController rootController;

    @BeforeEach
    void setup()
    {
        JacksonTester.initFields( this, JsonMapper.builder().build() );

        webTestClient = WebTestClient
            .bindToController( rootController )
            .configureClient()
            .baseUrl( BASE_URL )
            .build();
    }

    @DisplayName( "[root] => Should return correct api endpoints for operations and resources exposed by the Gateway" )
    @ParameterizedTest( name = "[{index}] => {0}" )
    @ArgumentsSource( RootArgumentsProvider.class )
    void shouldReturnCorrectApiEndpoints( String aTN, RootTestCase aTC ) throws Exception
    {
        // given
        when( apiEndpoints.getEndpoints() ).thenReturn ( aTC.mockedApiEndpoints );

        // when
        FluxExchangeResult< String > response = webTestClient
            .get()
            .uri( "/api" )
            .exchange()
            .returnResult( String.class );

        // then
        assertEquals( aTC.expectedStatus, response.getStatus() );
        assertEquals( jsonTester.write( aTC.expectedRoot ).getJson(), response.getResponseBody().blockFirst() );
    }
}