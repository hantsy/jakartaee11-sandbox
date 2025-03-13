package library.lending.application;

import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import library.common.UseCase;
import library.lending.domain.*;

@UseCase
public class RentBookUseCase {
    private final LoanRepository loanRepository;
    private final Event<LoanCreated> loanCreatedEvent;

    public RentBookUseCase() {
    }

    @Inject
    public RentBookUseCase(LoanRepository loanRepository, Event<LoanCreated> loanCreatedEvent) {
        this.loanRepository = loanRepository;
        this.loanCreatedEvent = loanCreatedEvent;
    }

    public void execute(CopyId copyId, UserId userId) {
        // TODO: ensure rented copy is not rented again
        loanRepository.save(new Loan(copyId, userId, loanRepository));
        loanCreatedEvent.fireAsync(new LoanCreated(copyId));
    }
}
