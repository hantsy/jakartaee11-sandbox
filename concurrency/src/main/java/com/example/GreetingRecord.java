package com.example;

import java.time.LocalDateTime;

public record GreetingRecord(String name, LocalDateTime sentAt) {
}