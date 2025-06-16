package com.example.blog;

import jakarta.persistence.EnumeratedValue;

public enum ModerationStatus {
    PENDING(0),
    APPROVED(1),
    REJECTED(-1),
    NA(-2);

    @EnumeratedValue
    private final int value;

    ModerationStatus(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
