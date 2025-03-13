package library.catalog.domain;

import java.util.Objects;

public record BarCode(String code) {
    public BarCode {
        Objects.requireNonNull(code, "Bar code must not be null");
    }
}
