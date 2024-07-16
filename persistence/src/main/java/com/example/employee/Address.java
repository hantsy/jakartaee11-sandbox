package com.example.employee;

public record Address(
        String street,
        String city,
        String state,
        ZipCode zipCode
) {
}
