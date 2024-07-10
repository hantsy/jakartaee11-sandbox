package com.example.employee;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.List;

@ApplicationScoped
public class EmployeeRepository {

    @Inject
    EntityManager entityManager;

    public List<Employee> allEmployees() {
        return entityManager.createQuery("select p from Employee p", Employee.class)
                .getResultList();
    }
}
