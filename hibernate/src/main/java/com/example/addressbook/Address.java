package com.example.addressbook;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.util.Objects;

@Embeddable
public record Address(
        @Enumerated(value = EnumType.STRING)
        AddressType type,
        String line1,
        String line2,
        String city,
        String postalCode,
        String country
) {
    public Address {
        if (type == null) {
            type = AddressType.HOME;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return type == address.type;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(type);
    }
}
