package library.catalog.domain;


public interface BookSearchService {
    BookInformation search(Isbn isbn);
}