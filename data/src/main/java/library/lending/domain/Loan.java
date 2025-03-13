package library.lending.domain;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class Loan {
    @EmbeddedId
    private LoanId loanId;
    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "copy_id"))
    private CopyId copyId;
    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "user_id"))
    private UserId userId;
    private LocalDateTime createdAt;
    private LocalDate expectedReturnDate;
    private LocalDateTime returnedAt;

    @Version
    private Long version;

    Loan() {
    }

    public LoanId id() {
        return loanId;
    }

    public CopyId copyId() {
        return this.copyId;
    }

    public Loan(CopyId copyId, UserId userId, LoanRepository loanRepository) {
        Objects.requireNonNull(copyId, "copyId must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
        if (!loanRepository.isAvailable(copyId)) {
            throw new IllegalArgumentException("copy with id = " + copyId + " is not available");
        }
        this.loanId = new LoanId();
        this.copyId = copyId;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
        this.expectedReturnDate = LocalDate.now().plusDays(30);
    }

    public void returned() {
        this.returnedAt = LocalDateTime.now();
        if (this.returnedAt.isAfter(expectedReturnDate.atStartOfDay())) {
            // calculate fee
        }
    }
}
