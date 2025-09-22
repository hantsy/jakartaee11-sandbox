# Jakarta Validation

[Jakarta Validation (formerly known as Jakarta Bean Validation) 3.1](https://jakarta.ee/specifications/bean-validation/3.1/) mainly adds support for record types, which were introduced in Java SE 11.

Let's use an example to illustrate this.

```java
// Customer.java
public record Customer(
    @NotBlank
    String firstName,
    @NotBlank
    String lastName,

    @NotNull
    Optional<@NotNull PhoneNumber> phoneNumber,

    @NotEmpty
    EmailAddress[] emailAddresses,

    @Valid
    Address address
) {
}

// Address.java
public record Address(
    @NotBlank
    String street,
    @NotBlank
    String city,
    String state,
    @NotBlank
    String zipCode
) {
}

// PhoneNumber.java
public record PhoneNumber(
    @NotBlank
    @Pattern(regexp = "\\d{3,4}")
    String countryCode,

    @NotBlank
    @Pattern(regexp = "\\d{3,11}")
    String number
) {
}

// EmailAddress.java
public record EmailAddress(
    @Email
    @NotBlank
    String email,
    @NotNull
    Boolean primary
) {
}
```

In a Java SE environment, simply add the Jakarta Validation implementation, eg, [Hibernate Validator](https://hibernate.org/validator/), to the classpath and initialize a `Validator` instance as follows:

```java
Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
```

Create a valid `Customer` instance and validate it using the `validator.validate` method:

```java
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
```

In the above example, the `constraints` result is empty.

Now, create a new `Customer` instance with a null phone number:

```java
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
```

When you run the code, the console will print the `constraints` as follows:

```bash
INFO: Constraints: [ConstraintViolationImpl{interpolatedMessage='must not be null', propertyPath=phoneNumber, rootBeanClass=class com.example.Customer, messageTemplate='{jakarta.validation.constraints.NotNull.message}'}]
```

Next, create another `Customer` instance without any emails:

```java
var data = new Customer(
    "Foo",
    "Bar",
    Optional.of(new PhoneNumber("001", "3334445555")),
    new EmailAddress[]{},
    new Address("123 Main St", "Anytown", "CA", "12345")
);

var constraints = validator.validate(data);
LOGGER.info("Constraints: " + constraints);
```

You should see the following output in the console:

```java
INFO: Constraints: [ConstraintViolationImpl{interpolatedMessage='must not be empty', propertyPath=emailAddresses, rootBeanClass=class com.example.Customer, messageTemplate='{jakarta.validation.constraints.NotEmpty.message}'}]
```

Now, create a new `Customer` with an address that does not have a zip code:

```java
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
```

Run the code and you will see the following output in the console:

```bash
INFO: Constraints: [ConstraintViolationImpl{interpolatedMessage='must not be blank', propertyPath=address.zipCode, rootBeanClass=class com.example.Customer, messageTemplate='{jakarta.validation.constraints.NotBlank.message}'}]
```

The `validator.validate` method returns a set of `ConstraintViolation` objects, which describe the validation constraints for each property path. If the set is empty, it means the validation was successful.

## Example Project

Download [the example project](https://github.com/hantsy/jakartaee11-sandbox/tree/master/validation) from GitHub and explore it on your local system. The [CustomerTest](https://github.com/hantsy/jakartaee11-sandbox/blob/master/validation/src/test/java/com/example/CustomerTest.java) class contains all the examples shown in this article.
