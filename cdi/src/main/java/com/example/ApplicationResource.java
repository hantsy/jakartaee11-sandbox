package com.example;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@RequestScoped
@Path(("app"))
public class ApplicationResource {

    @Inject
    ApplicationRecord applicationRecord;

    @GET
    public Record info() {
        return applicationRecord.info();
    }
}
