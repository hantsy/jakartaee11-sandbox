package com.example.employee;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.math.BigDecimal;

@Entity
@DiscriminatorValue("P")
public class ParttimeEmployee extends Employee{
    private BigDecimal hourlyRate = new BigDecimal("34.56");
}
