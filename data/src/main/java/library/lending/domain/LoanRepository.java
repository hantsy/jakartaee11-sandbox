package library.lending.domain;

import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;

@Repository
public interface LoanRepository extends CrudRepository<Loan, LoanId> {
    @Query("select count(*) = 0 from Loan where copyId = :id and returnedAt is null")
    boolean isAvailable(CopyId id);

    default Loan findByIdOrThrow(LoanId loanId) {
        return findById(loanId).orElseThrow();
    }
}
