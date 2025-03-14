package library.catalog.application;

import jakarta.inject.Inject;
import library.catalog.domain.*;
import library.common.UseCase;

@UseCase
public class AddBookToCatalogUseCase {
    private BookSearchService bookSearchService;
    private BookRepository bookRepository;

    // required by CDI spec
    AddBookToCatalogUseCase() {
    }

    @Inject
    public AddBookToCatalogUseCase(BookSearchService bookSearchService, BookRepository bookRepository) {
        this.bookSearchService = bookSearchService;
        this.bookRepository = bookRepository;
    }

    public void execute(Isbn isbn) {
        BookInformation result = bookSearchService.search(isbn);
        Book book = new Book(result.title(), isbn);
        bookRepository.save(book);
    }
}