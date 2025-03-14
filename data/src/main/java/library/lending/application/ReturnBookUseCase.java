package library.lending.application;

import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import library.common.UseCase;
import library.lending.domain.Loan;
import library.lending.domain.LoanClosed;
import library.lending.domain.LoanId;
import library.lending.domain.LoanRepository;
import org.eclipse.persistence.platform.database.oracle.plsql.PLSQLStoredFunctionCall;

import java.util.logging.Level;
import java.util.logging.Logger;

@UseCase
public class ReturnBookUseCase {
    private static final Logger LOGGER = Logger.getLogger(ReturnBookUseCase.class.getName());
    private LoanRepository loanRepository;
    private Event<LoanClosed> loanClosedEvent;

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

        LOGGER.log(Level.INFO, "firing returned event for loan with id = " + loanId);
        loanClosedEvent.fireAsync(new LoanClosed(loan.copyId()));
    }
}
