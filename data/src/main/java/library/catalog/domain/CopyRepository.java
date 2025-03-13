package library.catalog.domain;

import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Repository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@Repository
public interface CopyRepository extends CrudRepository<Copy, CopyId> {
}
