package library.lending.application;

import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import library.common.UseCase;
import library.lending.domain.*;

@UseCase
public class ReturnBookUseCase {

    private final LoanRepository loanRepository;
    private final Event<LoanClosed> loanClosedEvent;

    public ReturnBookUseCase() {
    }

    @Inject
    public ReturnBookUseCase(LoanRepository loanRepository,
                             Event<LoanClosed> loanClosedEvent) {
        this.loanRepository = loanRepository;
        this.loanClosedEvent = loanClosedEvent;
    }

    public void execute(LoanId loanId) {
        Loan loan = loanRepository.findByIdOrThrow(loanId);
        loan.returned();

        loanClosedEvent.fireAsync(new LoanClosed(loan.copyId()));
    }
}
