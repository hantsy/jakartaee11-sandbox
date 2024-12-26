package com.example.book;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;

import java.util.List;

@Singleton
//@Inject
public record BookService(EntityManager entityManager) {

    @Inject
    public BookService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    List<Book> getAllBooks() {
        return entityManager.createQuery("from Book", Book.class)
                .getResultList();
    }
}
