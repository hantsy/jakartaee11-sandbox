package com.example;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Optional;

public record Customer(
        @NotBlank
        String firstName,
        @NotBlank
        String lastName,

        @NotNull
        Optional<@NotNull PhoneNumber> phoneNumber,

        @NotEmpty
        EmailAddress[] emailAddresses,

        @Valid
        Address address
) {
}

