package com.example.chat;

import jakarta.validation.constraints.NotBlank;

public record NewMessageCommand(
        @NotBlank String body
) {
}
