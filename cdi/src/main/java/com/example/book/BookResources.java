package com.example.book;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import java.util.List;

@RequestScoped
@Path("books")
public class BookResources {

    @Inject BookService bookService;

    @GET
    public List<Book> allBooks() {
        return bookService.getAllBooks();
    }
}
