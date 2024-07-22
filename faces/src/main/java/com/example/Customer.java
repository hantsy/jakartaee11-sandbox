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

