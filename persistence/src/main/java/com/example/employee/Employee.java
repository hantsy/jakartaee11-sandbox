package com.example.employee;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.*;

@Entity
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;

    @Embedded
    private EmploymentPeriod employmentPeriod;

    @Embedded
    private PhoneNumber phoneNumber;

    // use new enum value in Gender
    private Gender gender = Gender.MALE;

    @Embedded
    private Address address;

    @ElementCollection
    private Set<Publication> publications = new HashSet<>();

    private Money salary;

    private Instant createdAt;

    @Version
    private Long version;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
    }

    public Employee() {
    }

    public Employee(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public EmploymentPeriod getEmploymentPeriod() {
        return employmentPeriod;
    }

    public void setEmploymentPeriod(EmploymentPeriod employmentPeriod) {
        this.employmentPeriod = employmentPeriod;
    }

    public PhoneNumber getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(PhoneNumber phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Money getSalary() {
        return salary;
    }

    public void setSalary(Money salary) {
        this.salary = salary;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", employmentPeriod=" + employmentPeriod +
                ", phoneNumber=" + phoneNumber +
                ", gender=" + gender +
                ", address=" + address +
                ", salary=" + salary +
                ", createdAt=" + createdAt +
                '}';
    }
}
