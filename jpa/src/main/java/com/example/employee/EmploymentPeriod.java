package com.example.employee;

import java.time.LocalDate;

public record EmploymentPeriod(
        LocalDate startDate,
        LocalDate endDate
) {
}
