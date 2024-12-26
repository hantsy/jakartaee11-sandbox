package com.example.book;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Startup;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.stream.Stream;

@ApplicationScoped
@Transactional
public class DataInitializer {

    @Inject
    EntityManager entityManager;

    public void init(@Observes Startup startup) {
        var author = new Author("foo", "bar");
        Stream.of("Book one", "Book two")
                .map(it -> Book.of(it, author))
                .forEach(entityManager::persist);
    }
}
