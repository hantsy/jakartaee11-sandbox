package com.example.employee;

import java.time.LocalDate;

public record EmploymentPeriod(
        LocalDate startDate,
        LocalDate endDate
) {
    public EmploymentPeriod {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
    }
}
