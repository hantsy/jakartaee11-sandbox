package com.example.employee;

import jakarta.persistence.Embeddable;
import jakarta.persistence.ManyToOne;

@Embeddable
public record PhoneNumber(
        String countryCode,
        String number,
        @ManyToOne
        PhoneServiceProvider phoneServiceProvider
) {
}
