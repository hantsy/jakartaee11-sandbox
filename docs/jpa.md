# Jakarta Persistence

The Jakarta Persistence API (JPA) is the standard for persistence and object-relational mapping in Java environments. It provides a straightforward API for executing queries using the Jakarta Persistence Query Language (JPQL), as well as an alternative Criteria API for constructing type-safe queries in Java code.

Jakarta Persistence 3.2 introduces numerous enhancements and new features. For a comprehensive list, see the [Jakarta Persistence 3.2 specification page](https://jakarta.ee/specifications/persistence/3.2/).

## Programmatic Configuration

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

Then you could create a `EntityManagerFactory` instance like this:

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
// Query books where the author's name matches the customer's first and last name
em.createQuery("""
    select b from Book b cross join Customer c
    where b.author.name = c.firstName || ' ' || c.lastName
      and c.firstName = :firstName
      and c.lastName = :lastName
    """, Book.class)
    .setParameter("firstName", "Gavin")
    .setParameter("lastName", "King")
    .getResultStream()
    .forEach(book -> LOG.debug("author name equals customer name: {}", book));
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

## API Enhancements

### Functional Transaction Operations

Before 3.2, transaction boundaries were managed as follows:

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

Jakarta Persistence 3.2 introduces two new methods, `runInTransaction` and `callInTransaction`, on `EntityManagerFactory` to execute logic within a transactional context:

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

emf.callInTransaction(em -> em.createQuery("from Book", Book.class)
                             .getResultList())
    .forEach(book -> LOG.debug("saved book: {}", book));
```

- `runInTransaction` takes a functional interface and does not return a result, like a `Runnable`, making it suitable for mutating operations such as insert, update, or delete.
- `callInTransaction` executes code that returns a result, similar to a `Callable`, and is ideal for queries.

With these methods, you no longer need to explicitly handle transaction operations, such as begin, commit, and rollback. Every execution block is automatically wrapped in a transaction boundary. 

The `EntityManager` also introduces a new method for binding Database operations to an immutable `Connection` object:

```java
em.runWithConnection((Connection conn) -> {
    var rs = conn.prepareStatement("select * from posts").executeQuery();
    while (rs.next()) {
        LOG.debug("query result:");
        LOG.debug("id: {}", rs.getLong("id"));
        LOG.debug("title: {}", rs.getString("title"));
        LOG.debug("content: {}", rs.getString("content"));
    }
});
```

There is no need to manage the `Connection` lifecycle yourself, but be careful not to close it inside the execution block.
