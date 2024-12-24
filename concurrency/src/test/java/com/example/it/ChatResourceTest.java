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

import com.example.ChatMessage;
import com.example.ChatResource;
import com.example.NewMessageCommand;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.sse.SseEventSource;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.in;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ArquillianExtension.class)
public class ChatResourceTest {

    private final static Logger LOGGER = Logger.getLogger(ChatResourceTest.class.getName());

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        File[] extraJars = Maven
                .resolver()
                .loadPomFromFile("pom.xml")
                .importCompileAndRuntimeDependencies()
                .resolve("org.assertj:assertj-core", "io.lettuce:lettuce-core")
                .withTransitivity()
                .asFile();
        var war = ShrinkWrap.create(WebArchive.class, "test.war")
                .addAsLibraries(extraJars)
                .addPackage(ChatResource.class.getPackage())
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        LOGGER.log(Level.INFO, "war deployment: {0}", new Object[]{war.toString(true)});
        return war;
    }

    @ArquillianResource
    private URL baseUrl;

    Client client;

    @BeforeEach
    public void before() {
        LOGGER.log(Level.INFO, "baseURL: {0}", new Object[]{baseUrl.toExternalForm()});
        client = ClientBuilder.newClient();
        //client.register(JsonbContextResolver.class);
    }

    @AfterEach
    public void after() {
        client.close();
    }

    @Test
    @RunAsClient
    @Order(1)
    public void testSendMessages() throws Exception {
        var target = client.target(URI.create(baseUrl.toExternalForm() + "api/chat"));
        try (SseEventSource eventSource = SseEventSource.target(target).build()) {

            // EventSource#register(Consumer<InboundSseEvent>)
            // Registered event handler will print the received message.
            eventSource.register(inboundSseEvent -> {
                var data =inboundSseEvent.readData(ChatMessage.class);
                LOGGER.info("received data:" + data);
            });

            // Subscribe to the event stream.
            eventSource.open();

            // send chat messages
            IntStream.rangeClosed(1, 30).forEach(i -> {
                try (Response r = target.request().post(Entity.json(new NewMessageCommand("test " + i)))) {
                    LOGGER.log(Level.INFO, "Get messages response status: {0}", r.getStatus());
                    assertEquals(204, r.getStatus());
                }
            });

            // Consume events for just 500 ms
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    @RunAsClient
    @Order(2)
    public void testMessages() throws Exception {
        var target = client.target(URI.create(baseUrl.toExternalForm() + "api/chat/sync"));
        String jsonString;
        try (Response r = target.request().accept(MediaType.APPLICATION_JSON_TYPE).get()) {
            LOGGER.log(Level.INFO, "Get messages response status: {0}", r.getStatus());
            assertEquals(200, r.getStatus());
            jsonString = r.readEntity(String.class);
        }
        LOGGER.log(Level.INFO, "Get messages result string: {0}", jsonString);
        assertThat(jsonString).doesNotContain("email");
        assertThat(jsonString).contains("body");
        assertThat(jsonString).contains("sent_at");
    }

    @Test
    @RunAsClient
    @Order(3)
    public void testMessagesAsync() throws Exception {
        var target = client.target(URI.create(baseUrl.toExternalForm() + "api/chat/async"));
        String jsonString;
        try (Response r = target.request().accept(MediaType.APPLICATION_JSON_TYPE).get()) {
            LOGGER.log(Level.INFO, "Get messages response status: {0}", r.getStatus());
            assertEquals(200, r.getStatus());
            jsonString = r.readEntity(String.class);
        }
        LOGGER.log(Level.INFO, "Get messages result string: {0}", jsonString);
        assertThat(jsonString).doesNotContain("email");
        assertThat(jsonString).contains("body");
        assertThat(jsonString).contains("sent_at");
    }

    @Test
    @RunAsClient
    @Order(4)
    public void testGetMessagesFlow() throws Exception {
        var target = client.target(URI.create(baseUrl.toExternalForm() + "api/chat/flow"));
        String jsonString;
        try (Response r = target.request().accept(MediaType.APPLICATION_JSON_TYPE).get()) {
            LOGGER.log(Level.INFO, "Get messages response status: {0}", r.getStatus());
            assertEquals(200, r.getStatus());
            jsonString = r.readEntity(String.class);
        }
        LOGGER.log(Level.INFO, "Get messages result string: {0}", jsonString);
        assertThat(jsonString).doesNotContain("email");
        assertThat(jsonString).contains("body");
        assertThat(jsonString).contains("sent_at");
    }

}