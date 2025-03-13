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

import com.example.Comment;
import com.example.CommentRepository;
import com.example.Post;
import com.example.PostRepository;
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

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ArquillianExtension.class)
public class LibraryTest {

    private final static Logger LOGGER = Logger.getLogger(LibraryTest.class.getName());

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class)
                .addPackages(true, "library")
                .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        LOGGER.log(Level.INFO, war.toString(true));
        return war;
    }

    @PersistenceContext
    private EntityManager em;

    @Inject
    private PostRepository postRepository;

    @Inject
    CommentRepository commentRepository;

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
        var post = new Post();
        post.setTitle("My Post");
        post.setContent("My Post Content");
        startTx();
        em.persist(post);
        endTx();
        assertNotNull(post.getId());

        var found = postRepository.findById(post.getId());
        assertTrue(found.isPresent());
        assertEquals(post.getTitle(), found.get().getTitle());

        postRepository.delete(found.get());
        found = postRepository.findById(post.getId());
        assertFalse(found.isPresent());

        var comment = new Comment();
        comment.setContent("My Comment");
        comment.setPost(post);
        startTx();
        em.persist(comment);
        endTx();
        assertNotNull(comment.getId());

        var foundComment = commentRepository.findById(comment.getId());
        assertTrue(foundComment.isPresent());
        assertEquals(comment.getContent(), foundComment.get().getContent());

        commentRepository.delete(foundComment.get());
        foundComment = commentRepository.findById(comment.getId());
        assertFalse(foundComment.isPresent());
    }


}
