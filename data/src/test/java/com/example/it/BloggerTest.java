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

import com.example.repository.Blogger;
import com.example.domain.Post;
import com.example.repository.DataInitializer;
import com.example.repository.PostRepository;
import jakarta.data.Limit;
import jakarta.data.Order;
import jakarta.data.Sort;
import jakarta.data.page.PageRequest;
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

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.example.domain.Status.DRAFT;
import static com.example.domain.Status.PUBLISHED;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ArquillianExtension.class)
public class BloggerTest {

    private final static Logger LOGGER = Logger.getLogger(BloggerTest.class.getName());

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class)
                .addPackage(Post.class.getPackage())
                .addPackage(Blogger.class.getPackage())
                .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        LOGGER.log(Level.INFO, war.toString(true));
        return war;
    }

    @PersistenceContext
    private EntityManager em;

    @Inject
    private Blogger blogger;

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
            LOGGER.log(Level.WARNING, "Transaction error: {0}", e.getMessage());
            ux.rollback();
        }
    }

    @Test
    public void testBlogger() throws Exception {
        var post = new Post();
        post.setTitle("My Post");
        post.setContent("My Post Content");
        startTx();
        blogger.insert(post);
        endTx();
        UUID postId = post.getId();
        LOGGER.log(Level.INFO, "insert post: {0}", new Object[]{postId});
        assertNotNull(postId);

        var foundPost = blogger.byId(postId);
        assertTrue(foundPost.isPresent());
        Post savedPost = foundPost.get();
        assertEquals(post.getTitle(), savedPost.getTitle());
        assertEquals(post.getContent(), savedPost.getContent());

        var foundByStatus = blogger.byStatus(
                DRAFT,
                Order.by(Sort.desc("createdAt")),
                Limit.of(10)
        );
        assertEquals(3, foundByStatus.size());

        // jakarta data page number starts with 1, NOTTTTTTT 0, I am crazy...
        var allPosts = blogger.allPosts("%My%", PageRequest.ofPage(1, 10, true));
        assertEquals(1, allPosts.totalElements());
        assertEquals(postId, allPosts.content().getFirst().id());

        savedPost.setTitle("New Title");
        savedPost.setStatus(PUBLISHED);
        startTx();
        blogger.update(savedPost);
        endTx();

        var foundByStatusAfterUpdated = blogger.byStatus(
                DRAFT,
                Order.by(Sort.desc("createdAt")),
                Limit.of(10)
        );
        assertEquals(2, foundByStatusAfterUpdated.size());

        var updatedPost = blogger.byId(postId);
        assertTrue(updatedPost.isPresent());
        assertEquals("New Title", updatedPost.get().getTitle());
    }

}
