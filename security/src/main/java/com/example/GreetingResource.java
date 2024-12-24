package com.example;


import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.SecurityContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@RequestScoped
@Path("hello")
@Authorized(roles = {"rest"})
public class GreetingResource {

    @Inject
    SecurityContext securityContext;

    @GET
    @Path("")
    public String hello() {
        return "Hello " + securityContext.getCallerPrincipal().getName();
    }
}
