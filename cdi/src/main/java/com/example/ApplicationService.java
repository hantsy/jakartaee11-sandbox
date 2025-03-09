package com.example;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import jakarta.inject.Inject;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public record ApplicationService(ApplicationProperties properties) {

    public ApplicationService() {
        this(null);
    }
    
    public String hello(String name) {
        return "Hello " + name + " from " + properties.getName();
    }
}
