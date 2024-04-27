package com.example;

import com.example.bookstore.Author;
import com.example.bookstore.Book;
import com.example.bookstore.Isbn;
import jakarta.persistence.*;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BookstoreTest {
    private static final Logger LOG = LoggerFactory.getLogger(BookstoreTest.class);

    @Test
    public void testWithPersistenceXML() {
        try (var emf = Persistence.createEntityManagerFactory("bookstorePU")) {

            emf.runInTransaction(em -> {
                em.persist(new Book(new Isbn("9781932394887"), "Java Persistence with Hibernate", new Author("Gavin King")));

                // var book =
                //        em.find(Book.class, isbn,
                //                Map.of("jakarta.persistence.cache.retrieveMode",
                //                            CacheRetrieveMode.BYPASS,
                //                       "jakarta.persistence.query.timeout", 500,
                //                       "org.hibernate.readOnly", true);

                // type safe options
//                var result = em.find(Book.class, new Isbn("9781932394887"),
//                        CacheRetrieveMode.BYPASS, Timeout.seconds(500), LockModeType.READ);
//                LOG.debug("found book result: {}", result);

                // get persistent or detached instance
//                var ref = em.getReference(result);
//                LOG.debug("book ref: {}", ref);

                em.createQuery("from Book where title like '%Hibernate'", Book.class).getResultStream()
                        .forEach(book -> LOG.debug("found books:{}", book));
            });

            emf.callInTransaction(em -> em.createQuery("from Book", Book.class)
                            .getResultList())
                    .forEach(book -> LOG.debug("saved book: {}", book));
        }
    }

    @Test
    public void testPersistenceConfiguration() {
        PersistenceConfiguration configuration = new PersistenceConfiguration("bookstore")
                .provider(HibernatePersistenceProvider.class.getName())
                .nonJtaDataSource("java:global/jdbc/BookstoreData")
                .managedClass(Book.class)
                .managedClass(Author.class)
                .property(PersistenceConfiguration.LOCK_TIMEOUT, 5000)
                .property("hibernate.type.prefer_java_type_jdbc_types", true)
                .property(PersistenceConfiguration.JDBC_URL, "jdbc:h2:mem:db1;DB_CLOSE_DELAY=-1")
                .property(PersistenceConfiguration.JDBC_DRIVER, "org.h2.Driver")
                .property(PersistenceConfiguration.JDBC_USER, "sa");

        //val emf =configuration.createEntityManagerFactory();
        try (var emf = Persistence.createEntityManagerFactory(configuration)) {

            emf.runInTransaction(em -> {
                em.persist(new Book(new Isbn("9781932394887"), "Java Persistence with Hibernate", new Author("Gavin King")));
            });

            emf.callInTransaction(em -> em.createQuery("from Book", Book.class)
                            .getResultList())
                    .forEach(book -> LOG.debug("saved book: {}", book));
        }
    }
}


