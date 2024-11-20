package com.example.employee;

import jakarta.persistence.Embeddable;

@Embeddable
public record ZipCode(
        String zip,
        String plusFour
) {
}
