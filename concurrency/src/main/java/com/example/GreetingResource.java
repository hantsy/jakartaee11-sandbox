package com.example;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.concurrent.CompletionStage;

@Path("greeting")
@RequestScoped
public class GreetingResource {

    @Inject
    GreetingService greetingService;

    @GET
    @Path("{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public CompletionStage<Response> sayHello(@PathParam("name") String name) {
        return greetingService.greetAsync(name)
                .thenApplyAsync(greetingRecord -> Response.ok(greetingRecord).build());
    }
}
