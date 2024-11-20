package com.example.employee;

import jakarta.persistence.Embeddable;

@Embeddable
public record Address(
        String street,
        String city,
        String state,
        ZipCode zipCode
) {
}
