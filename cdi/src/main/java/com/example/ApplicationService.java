package com.example;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperties;

@ApplicationScoped
public record ApplicationService(ApplicationProperties properties) {
    public ApplicationService() {
        this(null);
    }

    @Inject
    public ApplicationService(@ConfigProperties ApplicationProperties properties) {
        this.properties = properties;
    }
    
    public String hello(String name) {
        return "Hello " + name + " from " + properties.getName();
    }
}
