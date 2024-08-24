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

import com.example.blog.Comment;
import com.example.blog.Post;
import com.example.blog.Comment_;
import com.example.blog.Post_;
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
public class BlogTest {

    private final static Logger LOGGER = Logger.getLogger(BlogTest.class.getName());

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class)
                .addPackage(Post.class.getPackage())
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

    private void doInTx(Runnable runnable) throws Exception {
        startTx();
        runnable.run();
        endTx();
    }

    @Test
    public void testPackageLevelIdGeneratorStrategy() throws Exception {
        doInTx(() -> {
            // persist new Post entity
            Post entity = new Post("What's new in Persistence 3.2?",
                    "dummy content of Jakarta Persistence 3.2");
            entity.addComment(new Comment("dummy comment by addComment method"));
            em.persist(entity);
            LOGGER.log(Level.INFO, "persisted Post: {0}", new Object[]{entity});

            // persist comment
            var comment = new Comment(entity, "dummy comment");
            em.persist(comment);
            LOGGER.log(Level.INFO, "persisted comment: {0}", new Object[]{comment});
        });

        doInTx(() -> {
            var entity = em.createQuery("from Post", Post.class).getResultList().getFirst();
            LOGGER.log(Level.INFO, "query result: {0}", new Object[]{entity});
            // query byTitle named query
            var result = em.createNamedQuery(Post_.QUERY_BY_TITLE, Post.class)
                    .setParameter("title", "What's new in Persistence 3.2?")
                    .getSingleResult();
            LOGGER.log(Level.INFO, "query byTitle result: {0}", new Object[]{result});

            // query withComments entityGraph
            Post result2 = (Post) em.find(em.getEntityGraph(Post_.GRAPH_WITH_COMMENTS), entity.getId());
            LOGGER.log(Level.INFO, "query withComments result: {0}", new Object[]{result2.getComments()});

            // query withComments entityGraph programmatically
            var postEntityGraph = em.createEntityGraph("withComments");
            postEntityGraph.addAttributeNode("comments");
            Post result3 = (Post) em.find(postEntityGraph, entity.getId());
            LOGGER.log(Level.INFO, "query withComments programmatically result: {0}", new Object[]{result3.getComments()});
        });

    }

}
