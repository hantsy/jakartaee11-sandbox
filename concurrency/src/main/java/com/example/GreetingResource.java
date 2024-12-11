package com.example;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;

@RequestScoped
public class GreetingResource {

    @Inject
    GreetingService greetingService;

    @GET
    @Path("async")
    @Produces(MediaType.APPLICATION_JSON)
    public CompletionStage<Response> sayHello(@QueryParam("name") String name) {
        return greetingService.greetAsync(name)
                .thenApplyAsync(greetingRecord -> Response.ok(greetingRecord).build());
    }

    @GET
    @Path("flow")
    @Produces(MediaType.APPLICATION_JSON)
    public Flow.Publisher<GreetingRecord> sayHelloFlow(@QueryParam("name") String name) {
        return greetingService.greetFlow(name);
    }
}
