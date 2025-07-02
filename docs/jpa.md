# Jakarta Persistence 

Jakarta Persistence API (JPA) is a standard for persistence and object-relational mapping in Java environments. It includes a collection of simple APIs to execute a literal query (also known as JPQL). Additionally, it contains alternative Criteria APIs to construct the query clause in type-safe Java code.

Jakarta Persistence 3.2 includes a large number of enhancements and additions. For more information, please refer to the feature list on the [Jakarta Persistence 3.2 specification page](https://jakarta.ee/specifications/persistence/3.2/).


## Programmatic Configuration

Before 3.2 in a Java SE environment, if you want to build an `EntityManagerFactory` object, you should construct a *persistence.xml* in the project `src/main/resources/META-INF` folder. 

The following is an example of a *persistence.xml*. 

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
<persistence>
```

Then, create an `EntityManagerFactory` as follows.

```java
var emf = Persistence.createEntityManagerFactory("bookstorePU");
```

In Jakarta Persistence 3.2, a new `PersistenceUnitConfiguration` is introduced to assemble properties using a `Builder` pattern. 

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

You can create the `EntityManagerFactory` using `PersistenceConfiguration.createEntityManagerFactory` method.

```java 
var emf = configuration.createEntityManagerFactory();
```

Or using `Persistence.createEntityManagerFactory(PersistenceConfiguration configuration)`.

```java

var emf = Persistence.createEntityManagerFactory(configuration);
```

### Schema Management

Before version 3.2, you could configure and export the database schema in the classic *persistence.xml* file as follows.

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

You can use `Persistence.generate(String persistenceUinit, Map<String Object> properties)` method in the specified path in the above properties. 

A new `SchemaManager` is introduced in 3.2, which allows you to validate and create or drop the database schema, clean up data, etc., against the persistence configuration of the application. 

```java
emf.getSchemaManager().validate();
emf.getSchemaManager().truncate();
emf.getSchemaManager().drop(true); // if ture, apply the changes on db
emf.getSchemaManager().create(true); // if true, apply the changes on db
```

However, the `SchemaManager` does not support exporting the schema into DDL scripts. 

> In 3.2, `Persistence.generate` does not have a variant to accept `PersistenceConfiguration` as input parameter - `Persistence.generate(PersistenceConfiguration)`. 


## JPQL Improvements

Jakarta Persistence 3.2 enhances the Jakarta Persistence Query Language and introduces new syntax, while also porting some functions from SQL (Structured Query Language).
 
### Query without select clause

The feature has existed in Hibernate for a long time,  and it is standardized in Jakarta Persistence 3.2. 

```java
 em.createQuery("from Book where name like '%Hibernate'", Book.class)
	.getResultStream()
	.forEach(book -> LOG.debug("query result without select:{}", book));
```

The `select` is omitted, and the above JPQL is equivalent to the following classic form:

```java
select b  from Book b ...
```

### New functions: `id(this)`, `count(this)`, `version(this)`

With this new function, you can query the ID, count, and version of the selected entity, for example. 
 
```java
var count = em.createQuery("select count(this) from Book")
		.getSingleResult();
LOG.debug(" count(this) result:{}", count);

// id and version function
em.createQuery("select id(this), version(this) from Book", Object[].class)
	.getResultList()
	.forEach(book -> LOG.debug("id and version result:{}", book));
```

### String concatenation

In Jakarta Persistence 3.2, it supports SQL-like `||` to concatenate string fields into a single field in the query result, for example.

```java
// query book author name equals customer firstName and lastName
em.createQuery("""
		select b  from Book b cross join Customer c
		where b.author.name = c.firstName ||' '|| c.lastName
		and c.firstName=:firstName
		and c.lastName=:lastName
		""", Book.class)
	.setParameter("firstName", "Gavin")
	.setParameter("lastName", "King")
	.getResultStream()
	.forEach(book -> LOG.debug("query book author name equals customer firstName and lastName: {}", book));
```

### Null handling in the `Order By` clause

The `nulls first` and `nulls last` features, which were previously part of the SQL standard, are now supported in JPQL.

For example: 

```java
// improved sort nulls first/last
em.createQuery("from Book order by name nulls first", Book.class)
	.getResultStream()
	.forEach(book -> LOG.debug("improved sort nulls first:{}", book));
```					

The above query prioritizes the `null` name-based result when executing the query.		

### Porting SQL functions: `left`, `right`, `cast`, `replace`

Some standard SQL functions, such as `left`, `right`, `cast`, and `replace`, are ported to JPQL, allowing developers to avoid using native queries in Jakarta Persistence code.

```java
em.createQuery("""
		select left(name, 5),
		right(name, 2),
		cast(price as Integer),
		replace(name ,' ','_'),
		name
		from Book
		""", Object[].class)
	.getResultStream()
	.forEach(book -> LOG.debug("new functions result:{}", Stream.of(book).toList()));	
```	

## API enhancements

### Functional Transaction Operation

Before version 3.2, you could control the transaction boundaries using the following code.

```java
EntityTransaction tx = em.getTransaction();
tx.begin();
try {
	// ...
	em.persist(entity);
} catch (Exception e) {
	tx.rollback();
} finally {
	tx.commit();
}
```

In Jakarta Persistence 3.2, two callback methods - `runInTransaction` and `callInTransaction`- have been added to `EntityManagerFactory` to wrap a unit of work within a transaction.  

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

* The `runInTransaction` accepts a functional interface that uses an `EntityManager` as the input parameter and does not return a result. The behavior is similar to a `Runnable` block. It is suitable for cases that involve executing mutation operations, such as updating and deleting entities.
* The `callInTransaction` returns the query execution result instead, which behaves like a `Callable` block. It is suitable for the selection query case.

No worries about the `begin` and `commit` operations of the transaction, as they are performed automatically at runtime. 

Similarly, the `EntityManager` includes a new method to bind operations on an immutable `Connection` object.

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

Do not worry about the lifecycle of the `Connection` object, and be careful not to close it within the execution block. 



