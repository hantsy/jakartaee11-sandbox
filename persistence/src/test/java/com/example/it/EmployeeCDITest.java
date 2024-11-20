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
package com.example.it;

import com.example.employee.*;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Status;
import jakarta.transaction.UserTransaction;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(ArquillianExtension.class)
public class EmployeeCDITest {

    private final static Logger LOGGER = Logger.getLogger(EmployeeCDITest.class.getName());

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class)
                .addPackage(Employee.class.getPackage())
                .addAsResource("test-persistence-cdi.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        LOGGER.log(Level.INFO, war.toString(true));
        return war;
    }

    // @PersistenceContext
    @Inject
    @MyCustom
    private EntityManager em;

    @Inject
    private EmployeeRepository employeeRepository;

    @Inject
    UserTransaction ux;

    private void startTx() throws Exception {
        ux.begin();
        em.joinTransaction();
    }

    @AfterEach
    public void after() throws Exception {
        endTx();
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

    @Test
    public void testEmployeeCurd() throws Exception {
        var entity = new Employee("foo", "bar");
        var chinaUnicomProvider = new PhoneServiceProvider("China Unicom");
        var chinaMobileProvider = new PhoneServiceProvider("China Mobile");

        startTx();
        em.persist(chinaUnicomProvider);
        em.persist(chinaUnicomProvider);

        em.persist(entity);
        entity.setPhoneNumber(new PhoneNumber("86", "12345678", chinaMobileProvider));
        entity.setAddress(new Address("S street", "Boston", "MA", new ZipCode("abc", "0234")));
        entity.setGender(Gender.MALE);
        entity.setEmploymentPeriod(new EmploymentPeriod(LocalDate.now().minusYears(3),
                LocalDate.now().minusDays(10)));
        entity.setSalary(new Money(new BigDecimal("5000"), Currency.getInstance("USD")));
        em.flush();

        endTx();

        String queryString = """
                FROM Employee 
                """;
        var saved = em.createQuery(queryString, Employee.class)
                .getResultList()
                .getFirst();

        LOGGER.log(Level.INFO, "Saved employee: {0}", saved);
        assertNotNull(saved.getId());
    }

    @Test
    public void testEmployeeCrudWithRepository() throws Exception {
        var entity = new Employee("foo", "bar");
        var chinaUnicomProvider = new PhoneServiceProvider("China Unicom");
        var chinaMobileProvider = new PhoneServiceProvider("China Mobile");

        startTx();
        em.persist(chinaUnicomProvider);
        em.persist(chinaMobileProvider);

        em.persist(entity);
        entity.setPhoneNumber(new PhoneNumber("86", "12345678", chinaMobileProvider));
        entity.setAddress(new Address("S street", "Boston", "MA", new ZipCode("abc", "0234")));
        entity.setGender(Gender.MALE);
        entity.setEmploymentPeriod(new EmploymentPeriod(LocalDate.now().minusYears(3),
                LocalDate.now().minusDays(10)));
        entity.setSalary(new Money(new BigDecimal("5000"), Currency.getInstance("USD")));
        em.flush();

        endTx();

        var saved = employeeRepository.allEmployees().getFirst();

        LOGGER.log(Level.INFO, "Saved employee: {0}", saved);
        assertNotNull(saved.getId());
    }

}
