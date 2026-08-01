package com.example.record;

import jakarta.persistence.Embeddable;

@Embeddable
public record RecordId(String id) {
}
