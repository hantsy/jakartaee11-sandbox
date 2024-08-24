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

import com.example.record.MyEmbeddedEntity;
import com.example.record.MyEmbeddedIdEntity;
import com.example.record.MyIdClassEntity;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.logging.Level;
import java.util.logging.Logger;

@ExtendWith(ArquillianExtension.class)
public class RecordTest {

    private final static Logger LOGGER = Logger.getLogger(RecordTest.class.getName());

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addPackage(MyEmbeddedEntity.class.getPackage())
                .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @PersistenceContext
    private EntityManager em;

    @Inject
    private UserTransaction ux;


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
            LOGGER.log(Level.WARNING, "Failed to commit transaction", e);
            ux.rollback();
        }
    }

    private void doInTx(Runnable runnable) throws Exception {
        startTx();
        runnable.run();
        endTx();
    }

    @Test
    public void testRecordIdClass() throws Exception {
        doInTx(() -> {
            // persist MyClassIdEntity
            MyIdClassEntity entity = new MyIdClassEntity(new MyIdClassEntity.MyIdClass("test1", "test2"));
            em.persist(entity);
            LOGGER.log(Level.INFO, "persisted MyClassIdEntity: {0}", new Object[]{entity});

            // persist MyEmbeddedIdEntity
            MyEmbeddedIdEntity entity2 = new MyEmbeddedIdEntity(new MyEmbeddedIdEntity.MyId("test1"));
            em.persist(entity2);
            LOGGER.log(Level.INFO, "persisted MyEmbeddedIdEntity: {0}", new Object[]{entity2});

            // persist MyEmbeddedEntity
            MyEmbeddedEntity entity3 = new MyEmbeddedEntity(new MyEmbeddedEntity.MyEmbedded("test1", 40));
            em.persist(entity3);
            LOGGER.log(Level.INFO, "persisted MyEmbeddedEntity: {0}", new Object[]{entity3});
        });
    }

}
