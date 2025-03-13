/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
 */
package library;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Status;
import jakarta.transaction.UserTransaction;
import library.catalog.domain.*;
import library.lending.application.RentBookUseCase;
import library.lending.application.ReturnBookUseCase;
import library.lending.domain.Loan;
import library.lending.domain.LoanRepository;
import library.lending.domain.UserId;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(ArquillianExtension.class)
public class LibraryTest {

    private final static Logger LOGGER = Logger.getLogger(LibraryTest.class.getName());

    @Deployment
    public static WebArchive createDeployment() {
        File[] extraJars = Maven
                .resolver()
                .loadPomFromFile("pom.xml")
                .importCompileAndRuntimeDependencies()
                .resolve("org.assertj:assertj-core", "org.awaitility:awaitility")
                .withTransitivity()
                .asFile();
        WebArchive war = ShrinkWrap.create(WebArchive.class)
                .addAsLibraries(extraJars)
                .addPackages(true, "library")
                .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        LOGGER.log(Level.INFO, war.toString(true));
        return war;
    }

    @PersistenceContext
    private EntityManager em;

    @Inject
    private UserTransaction ux;

    @Inject
    private BookRepository bookRepository;

    @Inject
    private CopyRepository copyRepository;

    @Inject
    private RentBookUseCase rentBookUseCase;

    @Inject
    private ReturnBookUseCase returnBookUseCase;

    @Inject
    private LoanRepository loanRepository;

    private void startTx() throws Exception {
        ux.begin();
        em.joinTransaction();
    }

    private void endTx() throws Exception {
        LOGGER.log(Level.INFO, "Transaction status: {0}", ux.getStatus());
        try {
            if (ux.getStatus() == Status.STATUS_ACTIVE) {
                ux.commit();
            }
        } catch (Exception e) {
            ux.rollback();
        }
    }

    private void withTx(Runnable action) throws Exception {
        try {
            startTx();
            action.run();
            endTx();
        } catch (Exception e) {
            ux.rollback();
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testLibraryCrud() throws Exception {
        withTx(() -> {
            // Add a new Book
            Book book = new Book("Effective Java", new Isbn("978-0134685991"));
            bookRepository.save(book);

            // Add some copies of the book
            Copy copy1 = new Copy(book.getId(), new BarCode("BC001"));
            Copy copy2 = new Copy(book.getId(), new BarCode("BC002"));
            copyRepository.save(copy1);
            copyRepository.save(copy2);
        });

        UserId userId = new UserId();

        withTx(() -> {
            var allCopies = copyRepository.findAll().toList();
            assertThat(allCopies.size()).isEqualTo(2);
            // Rent a book
            CopyId copyId = allCopies.getFirst().id();
            rentBookUseCase.execute(new library.lending.domain.CopyId(copyId.id()), userId);

            // Verify that if the book copy available
            await().atMost(5000, TimeUnit.MILLISECONDS).untilAsserted(() -> {
                copyRepository.findById(copyId).ifPresent(
                        copy -> assertThat(copy.isAvailable()).isFalse()
                );

                // rent again should throw exception
                assertThrows(Exception.class, () -> rentBookUseCase.execute(new library.lending.domain.CopyId(copyId.id()), userId));
            });
        });

        withTx(() -> {
            var allLoans = loanRepository.findAll().toList();
            assertThat(allLoans.size()).isEqualTo(1);

            // Retrieve Loan and Return the book
            Loan loan = loanRepository.findByIdOrThrow(allLoans.getFirst().id()); // Update this to the actual LoanID
            returnBookUseCase.execute(loan.id());

            // Verify that the book is now available
            await().atMost(5000, TimeUnit.MILLISECONDS).untilAsserted(() -> {
                copyRepository.findById(new library.catalog.domain.CopyId(loan.copyId().id())).ifPresent(
                        copy -> assertThat(copy.isAvailable()).isTrue()
                );

            });
        });
    }
}