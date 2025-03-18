# Java Record Support in Jakarta EE 11

Java records are a special kind of class designed to hold immutable data and automatically provide implementations for common methods such as `equals()`, `hashCode()`, and `toString()`.

In Jakarta EE 11, Java records are treated as first-class citizens. Specifications such as Jakarta Persistence, Jakarta Validation, and Jakarta Expression Language (EL) fully embrace and support Java records.

## Jakarta Persistence

Records can be used as embeddable fields and identifier fields within an entity.

For example, if the `@Entity` `People` has an embeddable field `address`, we can create the `@Embeddable` `Address` as a Java record:

```java
@Embeddable
public record Address(
    String street,
    String city,
    String zipCode
) {}
```

And then use it in the `People` entity:

```java
@Entity
public class People {
    @Id Long id;
    String name;
    @Embedded Address address;
    // setters and getters
}
```

Here is an example of using a Java record as the identifier of an entity.

First, define the `SocialSecurityNumber` record:

```java
record SocialSecurityNumber(String ssn) {}
```

Then, use it as the identifier in the `People` entity:

```java
@Entity
public class People {
    @EmbeddedId SocialSecurityNumber id;
    String name;
    // setters and getters
}
```

It can also be used as an `@IdClass` when combining several fields as the identifier of an entity.

For example, define the `PersonId` record:

```java
public record PersonId(Long id, String email) {}
```

Then, use it as the identifier in the `Person` entity:

```java
@Entity
@IdClass(PersonId.class)
public class Person {
    @Id Long id;
    @Id String email;
    String name;
    // setters and getters
}
```

> [!NOTE]
> Due to the dependency of the `@Entity` class on runtime proxies and the immutable nature of records, records cannot be used as entities.

## Jakarta Validation

Similar to normal Java classes, Jakarta Validation annotations can be applied to Java record fields. See the following examples.

```java
public record People(
    @NotNull Long id,
    @NotNull String name,
    @Email String email,
    @Valid Address address
) {

    record Address(
        @NotNull String street,
        @NotNull String city,
        @Pattern(regexp = "\\d{5}") String zipCode
    ) {}
}
```

Create an instance of `People` and use the `Validator` to check for any violations of the validation rules applied to its fields.

```java
var people = new People(1L, "Hantsy Bai", "hantsy.bai@example.com", new People.Address("123 Main St", "Anytown", "12345"));

var validator = Validation.buildDefaultValidatorFactory().getValidator();
var violations = validator.validate(people);

violations.stream().forEach(System.out::println);
```

> [!NOTE]
> In the Jakarta EE environment, validation checks are automatically triggered within various components, including EJBs, CDI beans, REST resources, and Jakarta Persistence entities.

## Jakarta Expression Language

In Jakarta EE 10 and earlier versions, to access the component fields of a Java record instance, you must use the accessor methods.

For example, using the `People` and `Address` records example from above:

```java
var elProcessor = new ELProcessor();
elProcessor.defineBean("people", new People(1L, "Hantsy Bai", "hantsy.bai@example.com", new People.Address("123 Main St", "Anytown", "12345")));
```

Before Jakarta EE 11, you could access the record fields using accessor methods.

```java
elProcessor.eval("people.address().street()");
```

In Jakarta EE 11, you can access the fields like other normal Java classes.

```java
elProcessor.eval("people.address.street");
```

> [!NOTE]
> Jakarta Faces relies on Jakarta Expression Language, allowing you to use records as properties in the backing bean for immutable data on Facelets pages.

## Jakarta JSON Binding

> [!WARNING]
> Jakarta JSON Binding (JSON-B) itself has not been updated to explicitly support Java records, see: [jakartaee/jsonb-api#278](https://github.com/jakartaee/jsonb-api/issues/278). However, implementations like [Eclipse Yasson](https://projects.eclipse.org/projects/ee4j.yasson) and [Apache Johnzon](https://johnzon.apache.org/) have already added support for Java records.

Like other normal Java classes, you can use JSONB to convert between JSON string and Java records.

Here is an example of converting a `People` object to JSON and converting it back.

First, create an instance of `People` and convert it to JSON:

```java
People people = new People(1L, "Hantsy Bai", "hantsy.bai@example.com", new People.Address("123 Main St", "Anytown", "12345"));

JsonbConfig config = new JsonbConfig().withFormatting(true);
Jsonb jsonb = JsonbBuilder.create(config);

String json = jsonb.toJson(people);
System.out.println(json);
```

To convert the JSON string back to a `People` object:

```java
People deserializedPeople = jsonb.fromJson(json, People.class);
System.out.println(deserializedPeople);
```

> [!NOTE]
> Jakarta REST implementations such as [Eclipse Jersey](https://eclipse-ee4j.github.io/jersey/) and [RedHat Resteasy](https://resteasy.dev/) using JSONB for HTTP message encoding/decoding support Java records due to JSONB capabilities.

## Summary

While most Jakarta specifications have been updated to support Java records, there are still some exceptions:

1. Jakarta REST's `@BeanParam` does not support Java records, see: [jakartaee/rest#913](https://github.com/jakartaee/rest/issues/913).
2. If a Jakarta Messaging payload contains a Java record property or it is a Java record, message sending will fail because Jakarta Messaging requires the payload to implement the `Serializable` interface, see: [jakartaee/messaging#343](https://github.com/jakartaee/messaging/issues/343).
3. CDI does not support Java records as beans, see: [jakartaee/cdi#832](https://github.com/jakartaee/cdi/issues/832).

We hope that these issues will be addressed in future Jakarta EE releases.

