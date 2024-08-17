package com.example.bookstore;


import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Year;

@Entity
public class Book {
    @EmbeddedId
    private Isbn  id;

    private String name;

    @Embedded
    @AttributeOverride(name = "name", column = @Column(name = "author"))
    private Author author;

    private BigDecimal price = BigDecimal.ZERO;

    private Year publicationYear;

    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();
    }

    public Book() {
    }

    public Book(Isbn id, String name, Author author, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.price = price;
    }

    public Isbn getId() {
        return id;
    }

    public void setId(Isbn id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Year getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(Year publicationYear) {
        this.publicationYear = publicationYear;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", author=" + author +
                ", price=" + price +
                ", publicationYear=" + publicationYear +
                ", createdAt=" + createdAt +
                '}';
    }
}

