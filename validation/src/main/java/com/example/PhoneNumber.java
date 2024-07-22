package com.example;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PhoneNumber(
        @NotBlank
        @Pattern(regexp = "\\d{3-4}")
        String countryCode,

        @NotBlank
        @Pattern(regexp = "\\d{3-11}")
        String number) {
}
