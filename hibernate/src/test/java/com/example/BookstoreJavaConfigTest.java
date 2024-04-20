package com.example;

import com.example.bookstore.Author;
import com.example.bookstore.Book;
import com.example.bookstore.Isbn;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceConfiguration;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BookstoreJavaConfigTest {
    private static final Logger LOG = LoggerFactory.getLogger(BookstoreJavaConfigTest.class);

    public static void main(String[] args) {
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


