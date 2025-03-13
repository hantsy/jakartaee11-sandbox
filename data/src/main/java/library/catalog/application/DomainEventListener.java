package library.catalog.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;
import library.catalog.domain.Copy;
import library.catalog.domain.CopyId;
import library.catalog.domain.CopyRepository;
import library.lending.domain.LoanClosed;
import library.lending.domain.LoanCreated;

@ApplicationScoped
public class DomainEventListener {
    private final CopyRepository copyRepository;

    public DomainEventListener() {
    }

    @Inject
    public DomainEventListener(CopyRepository copyRepository) {
        this.copyRepository = copyRepository;
    }

    public void onLoanCreated(@ObservesAsync LoanCreated event) {
        Copy copy = copyRepository.findById(new CopyId(event.copyId().id())).orElseThrow();
        copy.makeUnavailable();
        copyRepository.save(copy);
    }


    public void onLoanClosed(@ObservesAsync LoanClosed event) {
        Copy copy = copyRepository.findById(new CopyId(event.copyId().id())).orElseThrow();
        copy.makeAvailable();
        copyRepository.save(copy);
    }
}
