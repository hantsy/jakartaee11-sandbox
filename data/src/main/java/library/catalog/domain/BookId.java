package library.catalog.domain;

import java.util.Objects;
import java.util.UUID;

public record BookId(UUID id) {

    public BookId {
        Objects.requireNonNull(id, "id must not be null");
    }

    public BookId() {
        this(UUID.randomUUID());
    }
}