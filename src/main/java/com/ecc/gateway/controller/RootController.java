package com.ecc.gateway.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping( "/api" )
public class RootController
{
    @GetMapping
    public Mono< Map< String, Object > > root()
    {
        Map< String, Object > links = Map.of(
            "signup", Map.of( "href", "http://localhost:8082/api/auth/actions/signup" ) );
System.out.println("elo!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        return Mono.just( Map.of( "_links", links ) );
    }
}
