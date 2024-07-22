package com.example;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EmailAddress(
        @Email
        @NotBlank
        String email,
        @NotNull
        Boolean primary
) {
}
