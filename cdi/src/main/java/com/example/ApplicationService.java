package com.example;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import jakarta.inject.Inject;

@ApplicationScoped
@AllArgsConstructor(onConstructor_ = @Inject)
@NoArgsConstructor
public record ApplicationService(ApplicationProperties properties) {

    
    public String hello(String name) {
        return "Hello " + name + " from " + properties.getName();
    }
}
