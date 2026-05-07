package com.example.customer;

import jakarta.persistence.Embeddable;
import jakarta.persistence.ManyToOne;

@Embeddable
public record OrderItem(
        @ManyToOne
        Product product,
        int quantity
) {
}
