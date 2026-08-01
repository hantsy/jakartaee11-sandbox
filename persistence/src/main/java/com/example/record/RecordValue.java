package com.example.record;

import jakarta.persistence.Embeddable;

@Embeddable
public record RecordValue(String name, int age) {
}
