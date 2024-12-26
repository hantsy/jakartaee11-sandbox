package com.example;

import jakarta.enterprise.context.Dependent;
import org.eclipse.microprofile.config.inject.ConfigProperties;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ConfigProperties(prefix = "app")
@Dependent
public record ApplicationProperties(
        String name,
        String description,
        @ConfigProperty(name = "server.host", defaultValue = "localhost")
        String host
) {
}
