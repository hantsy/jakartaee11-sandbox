package com.example;

import jakarta.validation.constraints.NotBlank;

public record Address(
        @NotBlank
        String street,
        @NotBlank
        String city,
        String state,
        @NotBlank
        String zipCode
) {
}
