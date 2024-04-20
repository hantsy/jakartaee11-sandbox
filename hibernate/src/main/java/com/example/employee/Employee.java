package com.example.employee;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@MappedSuperclass
public class Employee {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String name;

    @Column(name = "age", comment = "age")
    private int age = 30;

    @Enumerated(EnumType.STRING)
    private Gender gender = Gender.MALE;
    private Integer yearsWorked = 2;
    private LocalDateTime birthDate;

    private LocalDateTime joinedDate;

    public Employee() {
    }

    public Employee(String name, int age) {
        assert age > 0;
        this.name = name;
        this.age = age;
    }

    public UUID getId() {
        return id;
    }

    public Integer getYearsWorked() {
        return yearsWorked;
    }

    public void setYearsWorked(Integer yearsWorked) {
        this.yearsWorked = yearsWorked;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public LocalDateTime getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDateTime birthDay) {
        this.birthDate = birthDay;
    }

    public LocalDateTime getJoinedDate() {
        return joinedDate;
    }

    public void setJoinedDate(LocalDateTime joinedDate) {
        this.joinedDate = joinedDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return age == employee.age
                && Objects.equals(id, employee.id)
                && Objects.equals(name, employee.name)
                && gender == employee.gender
                && Objects.equals(yearsWorked, employee.yearsWorked)
                && Objects.equals(birthDate, employee.birthDate)
                && Objects.equals(joinedDate, employee.joinedDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, age, gender, yearsWorked, birthDate, joinedDate);
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", gender=" + gender +
                ", yearsWorked=" + yearsWorked +
                ", birthDate=" + birthDate +
                ", joinedDate=" + joinedDate +
                '}';
    }
}