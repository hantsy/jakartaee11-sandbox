# Jakarta Persistence 

Jakarta Persistence (aka JPA) is a standard of persistence and O/R mapping in Java environments. It includes a collection of simple and stupid APIs to execute literal query (aka JPQL), and also contains alternative Criteria APIs to build the query clause in type-safe Java codes.

Jakarta Persistence 3.2 includes a large number of enhancements and additions, more info please refer to the feature list in [Jakarta Persistence 3.2 specification page](https://jakarta.ee/specifications/persistence/3.2/).


## Programmatic API 

### Fluent Configuration API

Before 3.2 in a Java SE environment, if you want to build a `EntityManagerFactory` object, you should construct a *persistence.xml* in the project `src/main/resources/META-INF` folder. 

The followng is a *persistence.xml* example. 

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

Then create a `EntityManagerFactory` like this.

```java
var emf = Persistence.createEntityManagerFactory("bookstorePU");
```

In Jakarta Persistence 3.2, a new `PersistenceUnitConfiguration` is added to assemble the properties in a `Builder` pattern. 

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

You can create the `EntityManagerFactory` like this.

```java 
var emf = configuration.createEntityManagerFactory();
```

Or  

```java

var emf = Persistence.createEntityManagerFactory(configuration);
```


### Declartive Transaction Boundary

Before 3.2, when you control the transaction boundaries.

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

In Jakarta Persistence 3.2, there are two callback methods `runInTransaction` and `callInTransaction` in `EntityManagerFactory`  to wrap unit of work in a transacation.  


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

The `runInTransaction` accept a `EntityManager` as input parameter, and a `Runnable` body as output, it is suitable the case that does not need to return a result. 
In reverse, the `callInTransaction` accept a `Callable` like body as output and return the execution result.

No worry about the transaction begin and commit, they are performed automaticially at execution time. 

Similarly, the `EntityManager` includes a new method to bind opertions on an immutable `Connection` object.

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

No worry about the lifecydle of the `Connection` object, do not close it in the execution block. 

### Schema Managment

Before 3.2, you can configure and export the database schema in the classic *persistence.xml* like this.


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

In Jakarta Persistence 3.2, 

Additionally, there is a `SchemaManager` avaiable and allow you to validate and create schema, clean up data, etc. 

```java
emf.getSchemaManager().validate();
emf.getSchemaManager().truncate();
emf.getSchemaManager().drop(true); // if ture, apply the changes on db
emf.getSchemaManager().create(true); // if true, apply the changes on db
```

Unfornately, the `SchemaManager` does not support to export schema into DDL scripts. 


## JPQL Improvements

 Jakarta Persistence 3.2 improves Jakarta Persistence Query Language and add some new syntax, also backport some functions and operations from SQL(Structured Query Language).
 

### Query with select clause

The feature exists in Hibernate for a long time, now it is standardized in Jakarta Persistence 3.2. 

```java
 em.createQuery("from Book where name like '%Hibernate'", Book.class)
	.getResultStream()
	.forEach(book -> LOG.debug("query result without select:{}", book));

```

The `select` is omitted there, the above JPQL is equivilent to following:

```java
select b  from Book b ...
```

### New functions: `id(this)`, `count(this)`, `version(this)`

Utilize with this new function, you can query the id, count and version of the selected entity, for example. 
 
```java
var count = em.createQuery("select count(this) from Book")
		.getSingleResult();
LOG.debug(" count(this) result:{}", count);

// id and version function
em.createQuery("select id(this), version(this) from Book", Object[].class)
	.getResultList()
	.forEach(book -> LOG.debug("id and version result:{}", book));
```

### String contaction

In Jakarta Persistence 3.2, it supports SQL like `|` to contact string fields into one field in the query reuslt. 

For example.

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

### Null handling in Order By clause

The `nulls first` and `nulls last` existed in SQL, now it is ported to JPQL.

For example: 

```java
// improved sort nulls first/last
em.createQuery("from Book order by name nulls first", Book.class)
	.getResultStream()
	.forEach(book -> LOG.debug("improved sort nulls first:{}", book));
```					

The above query makes null name based result come first when executing the select clause.		

### SQL functions: left, right, cast, replace

Some common SQL functions such as `left`, `right`, `cast`, `replace` are ported to JPQL, this will avoid us to use native query in Jakarta Persistence codes.

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


