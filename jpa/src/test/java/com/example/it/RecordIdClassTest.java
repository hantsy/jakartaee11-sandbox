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

import com.example.Person;
import com.example.RecordIdClass;
import com.example.RecordIdClassEntity;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ArquillianExtension.class)
public class RecordIdClassTest {

    private final static Logger LOGGER = Logger.getLogger(RecordIdClassTest.class.getName());

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(RecordIdClass.class, RecordIdClassEntity.class)
                .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
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
    public void testRecordIdClass() throws Exception {
        String id = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();
        var recordIdClass = new RecordIdClass(id, id2);

        var entity = new RecordIdClassEntity(id, id2, "test");

        startTx();
        em.persist(entity);
        em.flush();

        endTx();

        String queryString = """
                SELECT r FROM RecordIdClassEntity r
                """;
        var saved = em.createQuery(queryString, RecordIdClassEntity.class)
                .getResultList()
                .getFirst();

        assertEquals(id, saved.getId());
        assertEquals(id2, saved.getId2());

    }

}
