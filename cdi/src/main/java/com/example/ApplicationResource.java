package com.example;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.config.inject.ConfigProperties;

@RequestScoped
@Path(("app"))
public class ApplicationResource {

    @Inject
    @ConfigProperties
    ApplicationProperties appInfo;

    @GET
    public ApplicationProperties info() {
        return appInfo;
    }
}
