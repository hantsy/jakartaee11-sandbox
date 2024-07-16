package com.example;

public record Customer(
        String firstName,
        String lastName,
        EmailAddress[] emailAddresses,
        Address address
) {
}

