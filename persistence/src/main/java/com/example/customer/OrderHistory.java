package com.example.customer;

import jakarta.persistence.Embeddable;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Embeddable
public record OrderHistory(
        @OneToMany(mappedBy = "customer")
        List<Order> orders
) {
    public static OrderHistory EMPTY = new OrderHistory(Collections.emptyList());

    public OrderHistory {
        if(orders == null) {
            orders = new ArrayList<>();
        }
    }
}
