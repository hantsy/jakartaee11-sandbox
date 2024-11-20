package com.example.employee;

import jakarta.persistence.Embeddable;

import java.time.Year;

@Embeddable
public record Publication(String name, String publisher, Year publicationYear) {
}
