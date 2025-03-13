package library.catalog.infrastructure;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;
import library.catalog.domain.BookInformation;
import library.catalog.domain.BookSearchService;
import library.catalog.domain.Isbn;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class OpenLibraryBookSearchService implements BookSearchService {
    private static final Logger LOGGER = Logger.getLogger(OpenLibraryBookSearchService.class.getName());
    private static final String BASE_URL = "https://openlibrary.org/";
    private final Client client;

    public OpenLibraryBookSearchService() {
        this.client = ClientBuilder.newClient();
    }

    public BookInformation search(Isbn isbn) {
        var targetUri = UriBuilder
                .fromUri(BASE_URL + "isbn/{isbn}.json")
                .build(isbn.value());
        var target = this.client.target(targetUri);
        try (var response = target.request().accept(MediaType.APPLICATION_JSON_TYPE).get()) {
            var result = response.readEntity(OpenLibraryIsbnSearchResult.class);
            LOGGER.log(Level.FINEST, "Book search result: {0}", result);
            return new BookInformation(result.title());
        }
    }
}

