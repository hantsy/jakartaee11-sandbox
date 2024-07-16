package com.example;

public record Customer(
        String firstName,
        String lastName,
        EmailAddress[] emailAddresses,
        Address address
) {
}
record EmailAddress(
        String email,
        Boolean primary
) {
}
record Address(
        String street,
        String city,
        String state,
        String zipCode
) {
}