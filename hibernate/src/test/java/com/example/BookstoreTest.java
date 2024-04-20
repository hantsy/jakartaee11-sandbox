package com.example;

import com.example.bookstore.Author;
import com.example.bookstore.Book;
import com.example.bookstore.Isbn;
import jakarta.persistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BookstoreTest {
    private static final Logger LOG = LoggerFactory.getLogger(BookstoreTest.class);

    public static void main(String[] args) {
        try (var emf = Persistence.createEntityManagerFactory("bookstorePU")) {

            emf.runInTransaction(em -> {
                em.persist(new Book(new Isbn("9781932394887"), "Java Persistence with Hibernate", new Author("Gavin King")));
            });

            emf.callInTransaction(em -> em.createQuery("from Book", Book.class)
                            .getResultList())
                    .forEach(book -> LOG.debug("saved book: {}", book));
        }
    }
}


