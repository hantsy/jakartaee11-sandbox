package com.example.employee;

import jakarta.persistence.ManyToOne;

public record PhoneNumber(
        String countryCode,
        String number,
        @ManyToOne
        PhoneServiceProvider phoneServiceProvider
) {
}
