package com.example;

import jakarta.el.ELProcessor;

public class ELExample {
    public static void main(String[] args) {
        ELProcessor elProcessor = new ELProcessor();
        elProcessor.defineBean("customer", new Customer(
                        "Foo",
                        "Bar",
                        new EmailAddress[]{
                                new EmailAddress("foo@example.com", true),
                                new EmailAddress("bar@example.com", false)
                        },
                        new Address("123 Main St", "Anytown", "CA", "12345")
                )
        );

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
}
