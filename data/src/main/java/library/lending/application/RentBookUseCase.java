package library.lending.application;

import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import library.common.UseCase;
import library.lending.domain.*;

import java.util.logging.Level;
import java.util.logging.Logger;

@UseCase
public class RentBookUseCase {
    private static final Logger LOGGER = Logger.getLogger(RentBookUseCase.class.getName());
    private LoanRepository loanRepository;
    private Event<LoanCreated> loanCreatedEvent;

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

        LOGGER.log(Level.INFO, "firing LoanCreated with copy id = " + copyId);
        loanCreatedEvent.fireAsync(new LoanCreated(copyId));
    }
}
