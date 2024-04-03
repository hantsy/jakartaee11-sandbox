package com.example.bookstore;

import jakarta.persistence.PersistenceConfiguration;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bookstore {
    private static final Logger LOG = LoggerFactory.getLogger(Bookstore.class);

    public static void main(String[] args) {
        try (var emf = new PersistenceConfiguration("bookstore")
                .provider(HibernatePersistenceProvider.class.getName())
                .nonJtaDataSource("java:global/jdbc/BookstoreData")
                .managedClass(Book.class)
                .managedClass(Author.class)
                .property(PersistenceConfiguration.LOCK_TIMEOUT, 5000)
                .property("hibernate.type.prefer_java_type_jdbc_types", true)
                .property(PersistenceConfiguration.JDBC_URL, "jdbc:h2:mem:db1;DB_CLOSE_DELAY=-1")
                .property(PersistenceConfiguration.JDBC_DRIVER, "org.h2.Driver")
                .property(PersistenceConfiguration.JDBC_USER, "sa")
                .createEntityManagerFactory()) {

            emf.runInTransaction(em -> {
                em.persist(new Book("Java Persistence with Hibernate", new Author("Gavin King")));
            });

            emf.callInTransaction(em -> em.createQuery("from Book", Book.class)
                            .getResultList())
                    .forEach(book -> LOG.debug("saved book: {0}", book));
        }
    }
}


