package com.example.bookstore;


import jakarta.persistence.*;

@Entity
public class Book {
    //@Id
    @EmbeddedId
    private Isbn  id;

    private String name;

    @Embedded
    @AttributeOverride(name = "name", column = @Column(name = "author"))
    private Author author;

    public Book() {
    }

    public Book(Isbn id, String name, Author author) {
        this.id = id;
        this.name = name;
        this.author = author;
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

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", author=" + author +
                '}';
    }
}

