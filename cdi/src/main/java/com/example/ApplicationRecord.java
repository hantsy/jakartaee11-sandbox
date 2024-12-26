package com.example;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public record ApplicationRecord(
        ApplicationProperties info
) {
    @Inject
    public ApplicationRecord(ApplicationProperties info) {
        this.info = info;
    }
}
