package com.example;

import java.time.LocalDateTime;

public record ChatMessage(String body, LocalDateTime sentAt) {
    static ChatMessage of(String body) {
        return new ChatMessage(body, LocalDateTime.now());
    }
}