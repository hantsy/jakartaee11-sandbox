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
import jakarta.persistence.PersistenceContext;
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
public class EmployeeTest {

    private final static Logger LOGGER = Logger.getLogger(EmployeeTest.class.getName());

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class)
                .addClasses(
                        Address.class,
                        Employee.class,
                        EmploymentPeriod.class,
                        Gender.class,
                        Money.class,
                        MoneyConverter.class,
                        PhoneNumber.class,
                        PhoneServiceProvider.class,
                        Publication.class,
                        ZipCode.class
                )
                .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        LOGGER.log(Level.INFO, war.toString(true));
        return war;
    }

    @PersistenceContext
    private EntityManager em;

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
        em.persist(chinaMobileProvider);

        em.persist(entity);
        entity.setPhoneNumber(new PhoneNumber("86", "12345678", chinaMobileProvider));
        entity.setAddress(new Address("S street", "Boston", "MA", new ZipCode("abc", "0234")));
        entity.setGender(Gender.MALE);
        entity.setEmploymentPeriod(new EmploymentPeriod(LocalDate.now().minusYears(3),
                LocalDate.now().minusDays(10)));
        entity.setSalary(new Money(new BigDecimal("5000"), Currency.getInstance("USD")));
        entity.setEmail("FOObar@gmail.com");
        em.flush();

        endTx();

        String queryString = """
                FROM Employee ORDER BY LOWER(email) ASC, createdAt DESC NULLS FIRST
                """;
        var saved = em.createQuery(queryString, Employee.class)
                .getResultList()
                .getFirst();

        LOGGER.log(Level.INFO, "Saved employee: {0}", saved);
        assertNotNull(saved.getId());

        // id and version function
        String idFunQuery = """
                SELECT ID(this) FROM Employee WHERE email = 'FOObar@gmail.com'
                """;
        var idValue = em.createQuery(idFunQuery, Object.class)
                .getSingleResult();
        LOGGER.log(Level.INFO, "id(this): {0}", idValue);
        assertNotNull(idValue);

        String versionFunQuery = """
                SELECT VERSION(this) FROM Employee WHERE email = 'FOObar@gmail.com'
                """;
        var version = em.createQuery(versionFunQuery, Object.class)
                .getSingleResult();
        LOGGER.log(Level.INFO, "version(this): {0}", version);
        assertNotNull(version);

        // count function
        String countFunQuery = """
                SELECT COUNT(this) FROM Employee
                """;
        var count = em.createQuery(countFunQuery, Long.class)
                .getSingleResult();
        LOGGER.log(Level.INFO, "Count: {0}", count);
        assertNotNull(count);

        // firstname and last name concat, left, right
        String firstNameAndLastNameQuery = """
                SELECT firstName || ' ' || lastName, LEFT(firstName, 1), RIGHT(lastName, 2) FROM Employee WHERE email = 'FOObar@gmail.com'
                """;
        var firstNameAndLastName = em.createQuery(firstNameAndLastNameQuery, Object[].class)
                .getSingleResult();
        LOGGER.log(Level.INFO, "First name and last name: {0}", firstNameAndLastName);
        assertNotNull(firstNameAndLastName[0]);
        assertNotNull(firstNameAndLastName[1]);
        assertNotNull(firstNameAndLastName[2]);
    }
}
