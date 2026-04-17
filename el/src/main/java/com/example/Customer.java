package com.example;

import java.util.Optional;

public record Customer(
        String firstName,
        String lastName,
        Optional<PhoneNumber> phoneNumber,
        EmailAddress[] emailAddresses,
        Address address
) {
}

record PhoneNumber(String countryCode, String number) {
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

