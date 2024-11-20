package com.example;

import jakarta.el.ELProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class ELExampleTest {

    private ELProcessor elProcessor;

    @BeforeEach
    public void init() {
        elProcessor = new ELProcessor();
    }

    @Test
    public void testRecordInEL() {

        elProcessor.defineBean("customer", new Customer(
                        "Foo",
                        "Bar",
                        Optional.of(new PhoneNumber("001", "3334445555")),
                        new EmailAddress[]{
                                new EmailAddress("foo@example.com", true),
                                new EmailAddress("bar@example.com", false)
                        },
                        new Address("123 Main St", "Anytown", "CA", "12345")
                )
        );

        // access optional phonenumber
        String phoneNumber = elProcessor.eval("customer.phoneNumber.map(p-> '('.concat(p.countryCode).concat(')').concat(p.number)).orElse('NotFound')");
        System.out.println(phoneNumber);

        String firstName = elProcessor.eval("customer.firstName");
        String lastName = elProcessor.eval("customer.lastName");
        System.out.println(firstName);
        System.out.println(lastName);

        Integer emailLength = elProcessor.eval("customer.emailAddresses.length");
        System.out.println("email length = " + emailLength);

        // access email
        String email = elProcessor.eval("customer.emailAddresses[0].email");
        System.out.println("email = " + email);

        // access address
        String street = elProcessor.eval("customer.address.street");
        System.out.println("street = " + street);

        String state = elProcessor.eval("customer.address.state");
        System.out.println("state = " + state);

        String city = elProcessor.eval("customer.address.city");
        System.out.println("city = " + city);

        String zip = elProcessor.eval("customer.address.zipCode");
        System.out.println("zip = " + zip);
    }

    @Test
    public void testNullableAccessInEL() {
        elProcessor.defineBean("customer", new Customer(
                        "Foo",
                        "Bar",
                        Optional.empty(),
                        null,
                        null //
                )
        );

        // access optional phonenumber
        String phoneNumber = elProcessor.eval("customer.phoneNumber.map(p-> '('.concat(p.countryCode).concat(')').concat(p.number)).orElse('NotFound')");
        System.out.println("Phone number = " + phoneNumber);

        // access email
        String email = elProcessor.eval("customer.emailAddresses[0].email");
        System.out.println("email = " + email);

        // access address
        String zip = elProcessor.eval("customer.address.zipCode");
        System.out.println("zip = " + zip);
    }
}