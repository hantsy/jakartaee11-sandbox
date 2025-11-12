package com.example.employee;

import jakarta.persistence.EnumeratedValue;

public enum Gender {
    MALE("M"), FEMALE("F");

    @EnumeratedValue
    private final String symbol;

    Gender(String symbol) {
        this.symbol = symbol;
    }
}
