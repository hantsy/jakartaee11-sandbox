package com.example.blog;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Embeddable
public record Subscription(
        @ManyToOne
        @JoinColumn(name="subscriber_id")
        Subscriber subscriber,
        LocalDateTime subscribedAt
) {
    public Subscription {
        subscribedAt = LocalDateTime.now();
    }

    public static Subscription of(Subscriber subscriber) {
        return new Subscription(subscriber, null);
    }
}

