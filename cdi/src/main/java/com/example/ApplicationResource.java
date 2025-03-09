package com.example;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.config.inject.ConfigProperties;

@RequestScoped
@Path(("app"))
public class ApplicationResource {

    @Inject
    ApplicationService applicationService;

    @GET
    public String info(@QueryParam("name") String name) {
        return applicationService.hello(name);
    }
}
