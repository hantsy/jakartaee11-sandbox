package com.example.addressbook;

import jakarta.persistence.Embeddable;

@Embeddable
public record Address(
        String street,
        AddressType type,
        String city,
        String postalCode,
        String country
) {
    public Address {
        if (type == null) {
            type = AddressType.HOME;
        }
    }
}
