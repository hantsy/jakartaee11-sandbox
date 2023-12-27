package com.example;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;

@Path("greeting")
@RequestScoped
public class GreetingResource {

    @GET
    @Path("{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response sayHello(@PathParam("name") String name) {
        var to = name == null || name.isBlank() ? "Hantsy" : name;
        var person = new GreetingRecord(to, LocalDateTime.now());
        return Response.ok(person).build();
    }
}
