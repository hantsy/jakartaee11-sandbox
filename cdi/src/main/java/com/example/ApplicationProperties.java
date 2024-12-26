package com.example;

import org.eclipse.microprofile.config.inject.ConfigProperties;

@ConfigProperties(prefix = "app.")
public record ApplicationProperties(
        String name,
        String description
) {
}
