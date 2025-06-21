## Jakarta Persistence 

Jakarta Persistence (aka JPA) is a standard of persistence and O/R mapping in Java environments. It includes a collection of simple and stupid APIs to execute literal query (aka JPQL), and also contains alternative Criteria APIs to build the query clause in type-safe Java codes.

Jakarta Persistence 3.2 includes a large number of enhancements and additions, more info please refer the feature list of [Jakarta Persistence 3.2 specification page](https://jakarta.ee/specifications/persistence/3.2/).


# Programmatic API 

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


```





  via the new introduced  programmatic API instead of discovering  in the application archive. 



