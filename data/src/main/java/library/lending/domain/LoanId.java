package library.lending.domain;

import java.util.Objects;
import java.util.UUID;

public record LoanId(UUID id) {

    public LoanId {
        Objects.requireNonNull(id, "id must not be null");
    }

    public LoanId() {
        this(UUID.randomUUID());
    }
}
