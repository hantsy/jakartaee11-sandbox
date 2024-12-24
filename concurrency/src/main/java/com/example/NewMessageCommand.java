package com.example;

import jakarta.validation.constraints.NotBlank;

public record NewMessageCommand(
        @NotBlank String body
) {
}
