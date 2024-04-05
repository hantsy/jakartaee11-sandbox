package com.example.customers;

public record Address(
        String street,
        String city,
        String postalCode,
        String country
) {
}
