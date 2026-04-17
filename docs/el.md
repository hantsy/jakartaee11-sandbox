# Jakarta Expression Language

[Jakarta Expression Language 6.0](https://jakarta.ee/specifications/expression-language/6.0/) removes the dependency on `SecurityManager` and several deprecated APIs. It also introduces a number of notable improvements for developers:

* Arrays now support a new `length` property.
* Introduces support for `java.lang.Record` through the new `RecordELResolver`, which is enabled by default.
* Adds support for `java.lang.Optional` via the new `OptionalELResolver`, which is disabled by default.

Let's use an example to explain these.

```java
public record Customer(
        String firstName,
        String lastName,
        Optional<PhoneNumber> phoneNumber,
        EmailAddress[] emailAddresses,
        Address address
) {
}

record PhoneNumber(String countryCode, String number) {
}

record EmailAddress(
        String email,
        Boolean primary
) {
}

record Address(
        String street,
        String city,
        String state,
        String zipCode
) {
}
```

In a Java SE environment, you can initialize an `ELProcessor` and evaluate expressions as follows::

```java
var elProcessor = new ELProcessor();

// add OptionalELResolver to support Optional
elProcessor.getELManager().addELResolver(new OptionalELResolver());

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
// String phoneNumber = elProcessor.eval("customer.phoneNumber.map(p-> '('.concat(p.countryCode).concat(')').concat(p.number)).orElse('NotFound')");
String phoneNumber = elProcessor.eval("'('.concat(customer.phoneNumber.countryCode).concat(')').concat(customer.phoneNumber.number)");
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
```

In the above code, we use expression `customer.phoneNumber.countryCode` and `customer.phoneNumber.number` to access the `countryCode` and `number` properties of the `PhoneNumber` record, which is wrapped in an `Optional`. The `OptionalELResolver` allows us to access these properties directly without needing to check if the `Optional` is present or not.

If the customer.phoneNumber is `Optional.empty()`, the expression will evaluate to `null` instead of throwing an exception, which is a significant improvement in handling optional values in expressions.

The expression `customer.emailAddresses.length` demonstrates the new `length` property for arrays, which returns the length of the `emailAddresses` array.

The expressions `customer.firstName`, `customer.lastName`, `customer.address.street`, `customer.address.state`, `customer.address.city`, and `customer.address.zipCode` show how to access properties of the `Customer` record and its nested `Address` record using the new `RecordELResolver`.

Check the full example code on [Github](https://github.com/hantsy/jakartaee11-sandbox/tree/master/el) and explore if yourself.
