package library.lending.domain;

import java.util.Objects;
import java.util.UUID;

public record UserId(UUID id) {

    public UserId {
        Objects.requireNonNull(id, "id must not be null");
    }

    public UserId() {
        this(UUID.randomUUID());
    }
}