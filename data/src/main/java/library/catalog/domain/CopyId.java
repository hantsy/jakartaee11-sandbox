package library.catalog.domain;

import java.util.Objects;
import java.util.UUID;

public record CopyId(UUID id) {

    public CopyId {
        Objects.requireNonNull(id, "id must not be null");
    }

    public CopyId() {
        this(UUID.randomUUID());
    }
}