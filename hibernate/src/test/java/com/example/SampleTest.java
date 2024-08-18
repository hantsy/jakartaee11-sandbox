package com.example;

import com.example.blog.Post;
import com.example.bookstore.Author;
import com.example.bookstore.Book;
import com.example.bookstore.Isbn;
import com.example.customers.Customer;
import com.example.record.MyClassIdEntity;
import com.example.record.MyEmbeddedIdEntity;
import jakarta.persistence.*;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Map;


public class SampleTest {
    private static final Logger LOG = LoggerFactory.getLogger(SampleTest.class);

    @Test
    public void testWithPersistenceXML() {
        try (var emf = Persistence.createEntityManagerFactory("bookstorePU")) {

            emf.runInTransaction(em -> {
                Book entity = new Book(
                        new Isbn("9781932394887"),
                        "Java Persistence with Hibernate",
                        new Author("Gavin King"),
                        new BigDecimal("50.1234")
                );
                em.persist(entity);
                LOG.debug("persisted book: {}", entity);

                var book =
                        em.find(Book.class, new Isbn("9781932394887"),
                                Map.of("jakarta.persistence.cache.retrieveMode",
                                        CacheRetrieveMode.BYPASS,
                                        "jakarta.persistence.query.timeout", 500,
                                        "org.hibernate.readOnly", true)
                        );
                LOG.debug("found book with Map properties: {}", book);

                // type safe options
                var result = em.find(Book.class, new Isbn("9781932394887"),
                        CacheRetrieveMode.BYPASS,
                        Timeout.seconds(500),
                        LockModeType.READ);
                LOG.debug("found book result with type-safe options: {}", result);

                // get persistent or detached instance
                var ref = em.getReference(result);
                LOG.debug("book ref: {}", ref);
            });

            emf.callInTransaction(em -> em.createQuery("from Book", Book.class)
                            .getResultList())
                    .forEach(book -> LOG.debug("saved book: {}", book));

        }
    }


    @Test
    public void testJPQLImprovements() {
        try (var emf = Persistence.createEntityManagerFactory("bookstorePU")) {

            emf.runInTransaction(em -> {
                //persist new book
                Book entity = new Book(
                        new Isbn("9781932394887"),
                        "Java Persistence with Hibernate",
                        new Author("Gavin King"),
                        new BigDecimal("50.1234")
                );
                em.persist(entity);
                LOG.debug("persisted book: {}", entity);

                // query without select
                em.createQuery("from Book where name like '%Hibernate'", Book.class)
                        .getResultStream()
                        .forEach(book -> LOG.debug("query result without select:{}", book));

                // new functions
                // count(this): org.hibernate.query.SemanticException: Could not interpret path expression 'this'
                var count = em.createQuery("select count(b) from Book b")
                        .getSingleResult();
                LOG.debug(" count(this) result:{}", count);

                // id and version does not work
                // Function "ID" not found
                // Function "VERSION" not found;
                em.createQuery("select left(name, 5), right(name, 2),cast(publicationYear as Integer) from Book")
                        .getResultStream()
                        .forEach(book -> LOG.debug("new functions result:{}", book));

                // improved sort nulls first/last
                em.createQuery("from Book order by name nulls first", Book.class)
                        .getResultStream()
                        .forEach(book -> LOG.debug("improved sort nulls first:{}", book));

                // persist new customers
                var customer = new Customer("Gavin", "King");
                em.persist(customer);
                LOG.debug("persisted customer: {}", customer);
                var customer2 = new Customer("Hantsy", "Bai");
                em.persist(customer2);
                LOG.debug("persisted customer2: {}", customer2);

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

                // query union book name and customer name
                em.createQuery("""
                                select c.firstName ||' '|| c.lastName from Customer c
                                union
                                select b.author.name  from Book b
                                """, String.class)
                        .getResultStream()
                        .forEach(name -> LOG.debug("query union book name and customer name: {}", name));

                // intersect book name and customer name
                em.createQuery("""
                                select c.firstName ||' '|| c.lastName from Customer c
                                intersect
                                select b.author.name  from Book b
                                """, String.class)
                        .getResultStream()
                        .forEach(name -> LOG.debug("intersect book name and customer name: {}", name));

                // except book name and customer name
                em.createQuery("""
                                select c.firstName ||' '|| c.lastName from Customer c
                                except
                                select b.author.name  from Book b
                                """, String.class)
                        .getResultStream()
                        .forEach(name -> LOG.debug("except book name and customer name: {}", name));

            });

        }
    }

    @Test
    public void testSchemaExport() {
        try (var emf = Persistence.createEntityManagerFactory("bookstorePU")) {
            // schema export
            LOG.debug("exporting schema...");
            emf.getSchemaManager().truncate();
            emf.getSchemaManager().drop(true);
            emf.getSchemaManager().create(true);
        }
    }

    @Test
    public void testMappingAnnotations() {
        try (var emf = Persistence.createEntityManagerFactory("bookstorePU")) {
            emf.runInTransaction(em -> {
                // persist new Post entity
                Post entity = new Post("What's new in Persistence 3.2?",
                        "dummy content of Jakarta Persistence 3.2");
                em.persist(entity);
                LOG.debug("persisted Post: {}", entity);

            });
        }
    }

    @Test
    public void testRecord() {
        try (var emf = Persistence.createEntityManagerFactory("bookstorePU")) {
            emf.runInTransaction(em -> {
                // persist MyClassIdEntity
                MyClassIdEntity entity = new MyClassIdEntity(new MyClassIdEntity.MyClassId("test1", "test2"));
                em.persist(entity);
                LOG.debug("persisted MyClassIdEntity: {}", entity);

                // persist MyEmbeddedIdEntity
                MyEmbeddedIdEntity entity2 = new MyEmbeddedIdEntity(new MyEmbeddedIdEntity.MyId("test1"));
                em.persist(entity2);
                LOG.debug("persisted MyEmbeddedIdEntity: {}", entity2);
            });
        }
    }

    @Test
    public void testPersistenceConfiguration() {
        PersistenceConfiguration configuration = new PersistenceConfiguration("bookstore")
                .transactionType(PersistenceUnitTransactionType.RESOURCE_LOCAL)
                .provider(HibernatePersistenceProvider.class.getName())
                // .nonJtaDataSource("java:global/jdbc/BookstoreData")
                .managedClass(Book.class)
                .managedClass(Author.class)
                .property(PersistenceConfiguration.LOCK_TIMEOUT, 5000)
                .property("hibernate.type.prefer_java_type_jdbc_types", true)
                .property("hibernate.hbm2ddl.auto", "create-drop")
                .property(PersistenceConfiguration.JDBC_URL, "jdbc:h2:mem:db1;DB_CLOSE_DELAY=-1")
                .property(PersistenceConfiguration.JDBC_DRIVER, "org.h2.Driver")
                .property(PersistenceConfiguration.JDBC_USER, "sa");

        // val emf =configuration.createEntityManagerFactory();
        try (var emf = Persistence.createEntityManagerFactory(configuration)) {

            emf.runInTransaction(em -> {
                Book entity = new Book(
                        new Isbn("9781932394887"),
                        "Java Persistence with Hibernate",
                        new Author("Gavin King"),
                        new BigDecimal("50.1234")
                );
                em.persist(entity);
            });

            emf.callInTransaction(em -> em.createQuery("from Book", Book.class)
                            .getResultList())
                    .forEach(book -> LOG.debug("saved book: {}", book));
        }
    }
}


