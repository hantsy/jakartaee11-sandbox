package com.example.customers;


import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name="value_customers")
public class ValueCustomer extends Customer{

        private Double discoundPercentage;
}
