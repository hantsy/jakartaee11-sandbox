package com.example.blog;

import jakarta.persistence.Embeddable;

import java.time.Instant;
import java.util.Objects;

@Embeddable
public record Slug(String slug) {

    public static Slug deriveFromTitle(String title) {
        Objects.requireNonNull(title, "Title requires none-null");
        // remove whitespaces in title and append a timestamp
        return new Slug(title.trim().replaceAll("(\\s)+", "-").toLowerCase() + "-" + Instant.now().getEpochSecond());
    }
}
