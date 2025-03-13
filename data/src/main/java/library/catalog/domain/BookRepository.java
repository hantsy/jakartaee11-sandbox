package library.catalog.domain;

import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Repository;

@Repository
public interface BookRepository extends CrudRepository<Book, BookId> {
}
