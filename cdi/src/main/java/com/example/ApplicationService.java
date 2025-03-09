package com.example;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperties;

@Singleton
public record ApplicationService(ApplicationProperties properties) {

    @Inject
    public ApplicationService {
    }


    public String hello(String name) {
        return "Hello " + name + " from " + properties.getName();
    }
}
