package com.example;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;


class CustomerTest {
    private static final Logger LOGGER = Logger.getLogger(CustomerTest.class.getName());

    Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }


    @Test
    void testCustomer() {
        var data = new Customer(
                "Foo",
                "Bar",
                Optional.of(new PhoneNumber("001", "3334445555")),
                new EmailAddress[]{
                        new EmailAddress("foo@example.com", true),
                        new EmailAddress("bar@example.com", false)
                },
                new Address("123 Main St", "Anytown", "CA", "12345")
        );

        var constraints = validator.validate(data);
        LOGGER.info("Constraints: " + constraints);
        assertThat(constraints).isEmpty();
    }

    // test if phonenumber is null
    @Test
    void testCustomerWithNullPhoneNumber() {
        var data = new Customer(
                "Foo",
                "Bar",
                Optional.empty(),
                new EmailAddress[]{
                        new EmailAddress("foo@example.com", true),
                        new EmailAddress("bar@example.com", false)
                },
                new Address("123 Main St", "Anytown", "CA", "12345")
        );

        var constraints = validator.validate(data);
        LOGGER.info("Constraints: " + constraints);
        assertThat(constraints).isNotEmpty();
    }

    // test if email address is empty
    @Test
    void testCustomerWithEmptyEmailAddress() {
        var data = new Customer(
                "Foo",
                "Bar",
                Optional.of(new PhoneNumber("001", "3334445555")),
                new EmailAddress[]{},
                new Address("123 Main St", "Anytown", "CA", "12345")
        );

        var constraints = validator.validate(data);
        LOGGER.info("Constraints: " + constraints);
        assertThat(constraints).isNotEmpty();
    }

    //test if address zipcode is null
    @Test
    void testCustomerWithNullZipCode() {
        var data = new Customer(
                "Foo",
                "Bar",
                Optional.of(new PhoneNumber("001", "3334445555")),
                new EmailAddress[]{
                        new EmailAddress("foo@example.com", true),
                        new EmailAddress("bar@example.com", false)
                },
                new Address("123 Main St", "Anytown", "CA", null)
        );

        var constraints = validator.validate(data);
        LOGGER.info("Constraints: " + constraints);
        assertThat(constraints).isNotEmpty();
    }

    @AfterEach
    void tearDown() {
    }
}