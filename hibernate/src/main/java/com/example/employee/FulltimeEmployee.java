package com.example.employee;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
public class FulltimeEmployee extends Employee {

    @Column(name = "salary", check = @CheckConstraint(name = "salary_check", constraint = "salary>0"))
    private BigDecimal salary;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "from", column = @Column(name = "employment_from")),
            @AttributeOverride(name = "to", column = @Column(name = "employment_to"))
    })
    private Employment employment;
}
