package com.example.book;

import jakarta.persistence.Embeddable;

@Embeddable
public record Author(String firstName, String lastName) {
}
