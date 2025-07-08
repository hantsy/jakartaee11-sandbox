# Jakarta Persistence

The Jakarta Persistence API (JPA) is the standard for persistence and object-relational mapping in Java environments. It provides a straightforward API for executing queries using the Jakarta Persistence Query Language (JPQL), as well as an alternative Criteria API for constructing type-safe queries in Java code.

Jakarta Persistence 3.2 introduces numerous enhancements and new features. For a comprehensive list, see the [Jakarta Persistence 3.2 specification page](https://jakarta.ee/specifications/persistence/3.2/).

## JPQL Improvements

Jakarta Persistence 3.2 refines the Jakarta Persistence Query Language (JPQL), introducing new syntax and porting several SQL functions.

### Queries Without a Select Clause

This feature, long available in Hibernate, is now standardized in Jakarta Persistence 3.2:

```java
em.createQuery("from Book where name like '%Hibernate'", Book.class)
    .getResultStream()
    .forEach(book -> LOG.debug("query result without select:{}", book));
```

Here, the `select` keyword is omitted. The query is equivalent to the classic form:

```java
select b from Book b ...
```

### New Functions: `id(this)`, `count(this)`, `version(this)`

Jakarta Persistence 3.2 introduces the functions `id(this)`, `count(this)`, and `version(this)`, allowing you to query an entity's ID, count, and version, respectively:

```java
var count = em.createQuery("select count(this) from Book")
    .getSingleResult();
LOG.debug("count(this) result: {}", count);

// id and version
em.createQuery("select id(this), version(this) from Book", Object[].class)
    .getResultList()
    .forEach(book -> LOG.debug("id and version result: {}", book));
```

### String Concatenation

JPQL now supports SQL-style string concatenation using `||`:

```java
// Query books where the author's name matches the person's first and last name
em.createQuery("""
        select b from Book b cross join Person c
        where b.author.name = c.firstName || ' ' || c.lastName
          and c.firstName = :firstName
          and c.lastName = :lastName
        """, Book.class)
    .setParameter("firstName", "Gavin")
    .setParameter("lastName", "King")
    .getResultStream()
    .forEach(book -> LOG.debug("author name equals person name: {}", book));
```

### Null Handling in the `ORDER BY` Clause

The `nulls first` and `nulls last` features, previously available only in SQL, are now supported in JPQL:

```java
em.createQuery("from Book order by name nulls first", Book.class)
    .getResultStream()
    .forEach(book -> LOG.debug("sorted with nulls first: {}", book));
```

This query ensures that results with `null` in the `name` field appear first.

### Additional SQL Functions: `left`, `right`, `cast`, `replace`

Standard SQL functions such as `left`, `right`, `cast`, and `replace` are now available in JPQL, reducing the need to fall back to native queries:

```java
em.createQuery("""
        select left(name, 5),
               right(name, 2),
               cast(price as Integer),
               replace(name, ' ', '_'),
               name
        from Book
        """, Object[].class)
    .getResultStream()
    .forEach(book -> LOG.debug("new functions result: {}", java.util.Arrays.toString(book)));
```

### Set Operations: `union`, `intersect`, and `except`

Several *set operators* in SQL, such as `union`, `intersect`, and `except`, have also been introduced in JPQL. These operators allow you to combine, compare, or subtract the results of two or more SELECT queries, treating the results as mathematical sets. Let’s examine some examples to illustrate their usage.

The following query combines person full names and book author names, returning a distinct list of all names.

```java
// query union book name and person name
em.createQuery("""
            select c.firstName ||' '|| c.lastName from Person c
            union
            select b.author.name  from Book b
            """, String.class)
    .getResultStream()
    .forEach(name -> LOG.debug("query union book name and person name: {}", name));
```

This query returns names that exist both as person full names and book author names.

```java
// intersect book name and person name
em.createQuery("""
            select c.firstName ||' '|| c.lastName from Person c
            intersect
            select b.author.name  from Book b
            """, String.class)
    .getResultStream()
    .forEach(name -> LOG.debug("intersect book name and person name: {}", name));
```

This query returns the person's full names that are not the book author's names.

```java
// except book name and person name
em.createQuery("""
            select c.firstName ||' '|| c.lastName from Person c
            except
            select b.author.name  from Book b
            """, String.class)
    .getResultStream()
    .forEach(name -> LOG.debug("except book name and person name: {}", name));
```

The Criteria API also added these changes in the *type-safe* Java form.  

## Entity Mapping Improvements

Jakarta Persistence 3.2 introduces several enhancements to the definition of Entity classes.

### Package-Level Generator Definitions

Before 3.2, when using `SequenceGenerator` or `TableGenerator`, you had to declare them with `@GeneratedValue` in the entity classes like this. 

```java
@Entity
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="blog_seq")
    @SequenceGenerator(name = "blog_seq", initialValue = 1, allocationSize = 10)
    private Long id;
    // ...
}
```

It can be a bit tedious to set up in every class.

Starting with version 3.2, Jakarta Persistence allows you to define identity generators at the package level. When a generator is declared in a `package-info.java` file, it will be automatically applied to all entity classes within that package.

For example, you can declare generators as follows in your `package-info.java`:

```java
@SequenceGenerator(name = "blog_seq", initialValue = 1, allocationSize = 10)
@TableGenerator(
    name = "tbl_id_gen",
    table = "id_gen",
    pkColumnName = "gen_key",
    pkColumnValue = "id",
    valueColumnName = "gen_val",
    allocationSize = 10
)
package com.example.blog;

import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.TableGenerator;
```

Once defined, your entity classes can simply reference these generators:

```java
@Entity
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    // ...
}

@Entity
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "tbl_id_gen")
    private Long id;
    // ...
}
```

The persistence provider will automatically discover the generators defined in `package-info.java` and apply them to the corresponding entities.

### Ongoing Java 8 Date and Time API Enhancements

With version 3.2, the support for legacy `Date`, `Calendar`, `Time`, and `java.sql.Date` types is now deprecated. It is recommended to use the modern Java 8 Date and Time API instead when starting a new project.

Additionally, `Instant` and `Year` are now supported as basic types:

```java
class Book {
    Instant createdAt;
    Year publicationYear;
}
```

Version 3.2 also introduces support for using `LocalDateTime` and `Instant` as the entity version type:

```java
class Book {
    @Version
    Instant version;
}
```

With the depreciation of the legacy date types, annotations such as `@Temporal` are also deprecated in 3.2.

### New Attributes in `@Column` Annotation

Jakarta Persistence 3.2 introduces two new attributes to the `@Column` annotation: `comment` and `check`, offering richer schema generation capabilities.

```java
@Entity
class Post {

    @Column(
        name = "title",
        nullable = false,
        length = 100,
        unique = true,
        comment = "Post title",
        check = @CheckConstraint(
            name = "title_min_length",
            constraint = "length(title) > 10"
        )
    )
    private String title;

    // ...
}
```

The new `check` attribute allows you to define check constraints at the column level, which will be reflected in the generated database schema:

```sql
title VARCHAR(100) NOT NULL UNIQUE /* Post title */ CHECK (length(title) > 10)
```

Another improvement in 3.2 is the `secondPrecision` attribute, which can be set on temporal columns to control the precision of persisted timestamp values. This is particularly useful for ensuring consistency across different persistence providers and various databases.

```java
@Column(name = "created_at", secondPrecision = 3)
private Instant createdAt;
```

This addresses previous issues where different JPA providers handled timestamp precision inconsistently. For example, I encountered this while contributing to [Eclipse Cargo Tracker](https://github.com/eclipse-ee4j/cargotracker/blob/master/src/main/java/org/eclipse/cargotracker/domain/model/voyage/CarrierMovement.java#L64).

### Customizing Enum Mapping with `@EnumeratedValue`

Before 3.2, Java enum types could only be mapped using their name or ordinal value with the `@Enumerated` annotation:

```java
@Entity
class Post {
    @Enumerated(EnumType.STRING)
    private ModerationStatus status;
    // ...
}

public enum ModerationStatus {
    PENDING,
    APPROVED,
    REJECTED
}
```

In 3.2, it introduces a new annotation - `@EnumeratedValue`, which allows you to specify a custom field of the Enum type to be persisted:

```java
@Entity
class Post {
    private ModerationStatus status;
    // ...
}

public enum ModerationStatus {
    PENDING(0),
    APPROVED(1),
    REJECTED(-1);

    @EnumeratedValue
    private final int value;

    ModerationStatus(int value) {
        this.value = value;
    }
}
```

Now, it will store the field value marked with `@EnumeratedValue` instead of the enum name or ordinal.


### Record Types as Embeddables

Record type support is a significant addition in Jakarta EE 11. With Jakarta Persistence 3.2, Java records are now fully supported and can be used as `@Embeddable` types. For more details, please refer to the dedicated [Java Record Support in Jakarta EE 11](./record.md) document.

## API Enhancements
### Programmatic Configuration

Before version 3.2, in a Java SE environment, creating an `EntityManagerFactory` required a *persistence.xml* file placed in the `src/main/resources/META-INF` directory of your project.

Here is an example of a *persistence.xml*:

```xml
<persistence xmlns="https://jakarta.ee/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="https://jakarta.ee/xml/ns/persistence https://jakarta.ee/xml/ns/persistence/persistence_3_2.xsd"
             version="3.2">

    <persistence-unit name="bookstorePU" transaction-type="RESOURCE_LOCAL">

        <description>Hibernate test case template Persistence Unit</description>
        <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>

        <exclude-unlisted-classes>false</exclude-unlisted-classes>

        <properties>
            <property name="hibernate.archive.autodetection" value="class, hbm"/>
            <property name="hibernate.dialect" value="org.hibernate.dialect.H2Dialect"/>
            <property name="hibernate.connection.driver_class" value="org.h2.Driver"/>
            <property name="hibernate.connection.url" value="jdbc:h2:mem:db1;DB_CLOSE_DELAY=-1"/>
            <property name="hibernate.connection.username" value="sa"/>
        </properties>
    </persistence-unit>
</persistence>
```

Then you could create an `EntityManagerFactory` instance like this:

```java
var emf = Persistence.createEntityManagerFactory("bookstorePU");
```

With Jakarta Persistence 3.2, the new `PersistenceUnitConfiguration` allows you to set up properties programmatically using the builder-style pattern:

```java
PersistenceConfiguration configuration = new PersistenceConfiguration("bookstore")
    .transactionType(PersistenceUnitTransactionType.RESOURCE_LOCAL)
    .provider(HibernatePersistenceProvider.class.getName())
    // .nonJtaDataSource("java:global/jdbc/BookstoreData")
    .managedClass(Book.class)
    .managedClass(Isbn.class)
    .managedClass(Author.class)
    .property(PersistenceConfiguration.LOCK_TIMEOUT, 5000)
    .property("hibernate.type.prefer_java_type_jdbc_types", true)
    .property("hibernate.hbm2ddl.auto", "create-drop")
    .property(PersistenceConfiguration.JDBC_URL, "jdbc:h2:mem:db1;DB_CLOSE_DELAY=-1")
    .property(PersistenceConfiguration.JDBC_DRIVER, "org.h2.Driver")
    .property(PersistenceConfiguration.JDBC_USER, "sa");
```

Then you can create an `EntityManagerFactory` instance using the following method:

```java
var emf = configuration.createEntityManagerFactory();
```

Or using the new `Persistence.createEntityManagerFactory` variant method, which accepts a `PersistenceConfiguration` as the input parameter:

```java
var emf = Persistence.createEntityManagerFactory(configuration);
```

### Schema Management

Before 3.2, you could configure and export the database schema using properties in *persistence.xml*:

```xml
<persistence ...>
    <persistence-unit>
        <properties>
            <property name="jakarta.persistence.schema-generation.scripts.action" value="drop-and-create" />
            <property name="jakarta.persistence.schema-generation.scripts.create-target" value="/tmp/create.ddl" />
            <property name="jakarta.persistence.schema-generation.scripts.drop-target" value="/tmp/drop.ddl" />
        </properties>
    </persistence-unit>
</persistence>
```

You could then use the `Persistence.generate(String persistenceUnit, Map<String, Object> properties)` method to generate schema scripts at the specified target paths.

Jakarta Persistence 3.2 introduces the new `SchemaManager`, which allows you to validate, create, drop, and truncate the database schema according to the application's persistence configuration:

```java
emf.getSchemaManager().validate();
emf.getSchemaManager().truncate();
emf.getSchemaManager().drop(true);    // if true, applies changes to the database
emf.getSchemaManager().create(true);  // if true, applies changes to the database
```

> [!Note]
> The `SchemaManager` does not support exporting the schema to DDL script files.
> In 3.2, the `Persistence.generate` does not involve a variant and accepts a `PersistenceConfiguration` as a parameter (i.e., `Persistence.generate(PersistenceConfiguration)` does not exist).


### Functional Transactions

Before 3.2, you could control the transaction boundaries manually in code as follows:

```java
EntityTransaction tx = em.getTransaction();
tx.begin();
try {
    // ...
    em.persist(entity);
    tx.commit();
} catch (Exception e) {
    tx.rollback();
}
```

Jakarta Persistence 3.2 introduces two new methods, `runInTransaction` and `callInTransaction`, on `EntityManagerFactory`, which allow you to execute logic within a transactional context.

The following is an example of persisting an `Entity` object and does not return a result. It is suitable for mutating operations such as insert, update, or delete.

```java
emf.runInTransaction(em -> {
    Book entity = new Book(
        new Isbn("9781932394887"),
        "Java Persistence with Hibernate",
        new Author("Gavin King"),
        new BigDecimal("50.1234")
    );
    em.persist(entity);
    LOG.debug("persisted book: {}", entity);
});
```

Alternatively, the `callInTransaction` method is designed to return a result after the logic is executed. It is ideal for selection queries.

```java
emf.callInTransaction(em -> em.createQuery("from Book", Book.class)
                             .getResultList())
    .forEach(book -> LOG.debug("saved book: {}", book));
```

With these methods, you no longer need to explicitly handle transaction operations, such as begin, commit, and rollback. Every execution block is automatically wrapped in a transaction boundary. 

Additionally, the `EntityManager` adds two similar methods: `runWithConnection` and `callWithConnection`, which bind database operations to an immutable `Connection` abstraction. For databases using JDBC, these methods let you work with a JDBC `Connection` object.

Here’s how to use `runWithConnection`:

```java
em.runWithConnection(conn -> {
    var rs = conn.prepareStatement("select * from posts").executeQuery();
    while (rs.next()) {
        LOG.debug("query result:");
        LOG.debug("id: {}", rs.getLong("id"));
        LOG.debug("title: {}", rs.getString("title"));
        LOG.debug("content: {}", rs.getString("content"));
    }
});
```

This method is transaction-aware and joins any existing transaction. You don’t need to manage transactions and care about the lifecycle of the input Connection object. Do not try to close the `Connection` yourself inside the block.

### Type-Safe Options

In Jakarta Persistence 3.2, the 'EntityManager' overloads methods such as `find`, `refresh`, and `lock` to accept type-safe `FindOption`, `RefreshOption`, and `LockOption` respectively, replacing the previous use of a generic `Map<String, Object>` for properties.

Before 3.2, you could tune the `find` method with a general `Map` parameter.

```java
// Using a Map for query hints and options
var book = em.find(Book.class, new Isbn("9781932394887"),
        Map.of("jakarta.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS,
               "jakarta.persistence.query.timeout", 500,
               "org.hibernate.readOnly", true)
);
LOG.debug("Found book using Map-based options: {}", book);
```

In 3.2, you can use the type-safe options instead. 

```java
// Using type-safe options
var result = em.find(Book.class, new Isbn("9781932394887"),
        CacheRetrieveMode.BYPASS,
        Timeout.seconds(500),
        LockModeType.READ);
LOG.debug("Found book using type-safe options: {}", result);
```

Besides these, the generated static metamodel includes strongly typed constants for named queries, entity graphs, result set mappings, and managed types, thereby minimizing typos and making refactoring safer.

Suppose you have the following entity:

```java
@NamedQuery(name = "byTitle", query = "SELECT p FROM Post p WHERE p.title = :title")
@NamedEntityGraph(
        name = "withComments",
        attributeNodes = {
                @NamedAttributeNode("title"),
                @NamedAttributeNode("content"),
                @NamedAttributeNode(value = "comments", subgraph = "commentsGraph"),
                @NamedAttributeNode("createdAt")
        },
        subgraphs = @NamedSubgraph(
                name = "commentsGraph",
                attributeNodes = @NamedAttributeNode("content")
        )
)
public class Post { ... }
```

After compilation, the generated `Post_` metamodel class will contain constants for the named query `byTitle` and named entity graph `withComments`:

```java
public abstract class Post_ {
    // ...
    public static final String QUERY_BY_TITLE = "byTitle";
    public static final String GRAPH_WITH_COMMENTS = "withComments";
}
```

Then you can use the constants for queries and entity graphs to replace the literal text.

```java
// Referencing the named query using the metamodel constant
var result = em.createNamedQuery(Post_.QUERY_BY_TITLE, Post.class)
        .setParameter("title", "What's new in Persistence 3.2?")
        .getSingleResult();
LOG.debug("Query byTitle result: {}", result);

// Referencing the named entity graph using the metamodel constant
Post result2 = em.find(Post.class, entity.getId(),
        em.getEntityGraph(Post_.GRAPH_WITH_COMMENTS));
LOG.debug("Query withComments result: {}", result2.getComments());
```

You can also refer to attribute names via the static metamodel in relationship mappings:

```java
@Entity
public class Post {
    // ...
    @OneToMany(mappedBy = Comment_.POST,
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<Comment> comments = new HashSet<>();
}
```

This approach ensures type safety and helps avoid errors caused by hard-coded string references.

### New Method `getSingleResultOrNull` in `Query`

Before version 3.2, the `getSingleResult` method would return the single unique result if it existed; otherwise, it would throw a `NoResultException`. To return `null` instead, you had to write something like:

```java
try {
   return query.getSingleResult();
} catch (NoResultException e) {
   return null;
}
```

Jakarta Persistence 3.2 solves this by introducing the new `getSingleResultOrNull` method to `Query` and its derived interfaces, including `TypedQuery<T>`, among others. This method returns `null` directly when no result is found.

Here is an example using `getSingleResultOrNull`:

```java
var nullableResult = em.createQuery("from Book where id = :isbn", Book.class)
        .setParameter("isbn", new Isbn("9781932394887"))
        .getSingleResultOrNull();
LOG.debug("book getSingleResultOrNull result: {}", nullableResult);
```

Now you never have to worry about catching a `NoResultException`.

> [!Note]
> When representing the presence or absence of a single result, I would prefer to use `Optional<T>` to align with modern Java best practices. Check the issue: [jakartaee/persistence#479](https://github.com/jakartaee/persistence/issues/479).

### New Method `getReference(T)` in `EntityManager`

As an alternative to the existing `getReference(Class, id)`, the new method provides a way to obtain a reference to an entity using a given object with the same primary key. The supplied object may be in a *managed* or *detached* state, but it must not be *new* or *removed*.

This method is beneficial when you need to set an association using detached entity instances, for example:

```java
var post = ...
// The session is closed here, so the `post` instance is now detached

// In a new session
comment.setPost(em.getReference(post));
// Persist the comment and close the session
```

This approach avoids the need to fetch the post entity from the database again.

Here, we highlight the API improvements in Jakarta Persistence 3.2 that offer tangible benefits to application developers. While there are many other minor enhancements not covered here, you can find the complete list of changes in the [Jakarta Persistence 3.2 specification](https://jakarta.ee/specifications/persistence/3.2/).

## Jakarta EE Integration

In Jakarta EE environments, you no longer need to use `@PersistenceUnit` or `@PersistenceContext` to inject `EntityManagerFactory` or `EntityManager` in the Jakarta EE components. Instead, you can use standard CDI `@Inject` to inject them like injecting regular CDI beans.

For example, before 3.2, you can use `@PersistenceContext` to inject an `EntityManager` bean that matches the default persistence unit definition in the *persistence.xml* file:

```java
@PersistenceContext
private EntityManager em;
```

In 3.2, you can use the CDI `@Inject` instead.

```java
@Inject
private EntityManager em;
```

Jakarta Persistence 3.2 also allows you to specify `scope` and `qualifier` elements in your *persistence.xml* file, making it easier to control the lifecycle and selection of persistence units in CDI.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence version="3.2" xmlns="https://jakarta.ee/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="https://jakarta.ee/xml/ns/persistence https://jakarta.ee/xml/ns/persistence/persistence_3_2.xsd">
    <persistence-unit name="defaultPU" transaction-type="JTA">
        <qualifier>com.example.employee.MyCustom</qualifier>
        <scope>jakarta.enterprise.context.ApplicationScoped</scope>
        <jta-data-source>java:comp/DefaultDataSource</jta-data-source>
        ...
    </persistence-unit>
</persistence>
```

Here, `MyCustom` is a custom CDI `@Qualifier` annotation:

```java
@Documented
@Retention(RUNTIME)
@Qualifier
public @interface MyCustom {
}
```

Then you can inject the qualified `EntityManager` or `EntityManagerFactory` as follows:

```java
@Inject @MyCustom
private EntityManager em;
```

## Example Projects

All [sample code](https://github.com/hantsy/jakartaee11-sandbox) referenced in this guide is available on GitHub, you can explore and try it out yourself.

### Hibernate Example Project

You can find the Hibernate example here: https://github.com/hantsy/jakartaee11-sandbox/tree/master/hibernate, which demonstrates running Jakarta Persistence code in Java SE environments.

Check out the source code, and import the project into your favorite IDE.

As you see, the project includes dependencies for Hibernate ORM and the Jakarta Persistence API:

```xml
<dependency>
    <groupId>org.hibernate.orm</groupId>
    <artifactId>hibernate-core</artifactId>
    <version>${hibernate.version}</version>
</dependency>
<dependency>
    <groupId>org.hibernate.orm</groupId>
    <artifactId>hibernate-scan-jandex</artifactId>
    <version>${hibernate.version}</version>
</dependency>
<dependency>
    <groupId>jakarta.persistence</groupId>
    <artifactId>jakarta.persistence-api</artifactId>
    <version>3.2.0</version>
</dependency>
```

To generate static metamodel classes for your entities, you should add `hibernate-processor` to the `annotationProcessorPaths` section of the `maven-compiler-plugin` configuration:

```xml
<plugins>
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>${maven-compiler-plugin.version}</version>
        <configuration>
            <annotationProcessorPaths>
                <annotationProcessorPath>
                    <groupId>org.hibernate.orm</groupId>
                    <artifactId>hibernate-processor</artifactId>
                    <version>${hibernate.version}</version>
                </annotationProcessorPath>
            </annotationProcessorPaths>
        </configuration>
    </plugin>
</plugins>
```

You can explore the test code in the project to see Jakarta Persistence 3.2 features in action.

### Jakarta EE Example Project

The Jakarta EE example is available at: https://github.com/hantsy/jakartaee11-sandbox/tree/master/persistence. This sample project demonstrates the integration of Jakarta Persistence 3.2 and CDI in a Jakarta EE environment, and it is designed to run on Jakarta EE application servers such as GlassFish 8.x or WildFly 37+.

In this project, you do not need to add an extra persistence provider dependency. Jakarta EE application servers provide it automatically at runtime.

Here we configured EclipseLink to generate static metamodel classes:

```xml
<plugins>
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>${maven-compiler-plugin.version}</version>
        <configuration>
            <parameters>true</parameters>
            <annotationProcessorPaths>
                <annotationProcessorPath>
                    <groupId>org.eclipse.persistence</groupId>
                    <artifactId>org.eclipse.persistence.jpa.modelgen.processor</artifactId>
                    <version>${eclipselink.version}</version>
                </annotationProcessorPath>
            </annotationProcessorPaths>
        </configuration>
    </plugin>
</plugins>
```

To run the tests on a *managed* Glassfish, execute:

```shell
mvn clean verify -Parq-managed-glassfish
```

It utilizes the GlassFish Managed Adapter for Arquillian and runs tests on the real GlassFish servers.

> [!NOTE]
> For more information about Arquillian, visit https://www.arquillian.org.

## Summary

In summary, Jakarta Persistence 3.2 introduces a range of enhancements, such as improved JPQL syntax, backported SQL functions, modernized support for Java Date and Time types, streamlined configuration options, and deeper integration with Jakarta EE and CDI. For comprehensive details, see the [Jakarta Persistence 3.2 specification](https://jakarta.ee/specifications/persistence/3.2/).
