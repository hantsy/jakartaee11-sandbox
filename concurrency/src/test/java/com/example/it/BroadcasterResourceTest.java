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

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.sse.SseEventSource;

import com.example.broadcast.BroadcasterResource;
import com.example.RestActivator;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ArquillianExtension.class)
public class BroadcasterResourceTest {

    private final static Logger LOGGER = Logger.getLogger(BroadcasterResourceTest.class.getName());

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        File[] extraJars = Maven
                .resolver()
                .loadPomFromFile("pom.xml")
                //.importCompileAndRuntimeDependencies()
                .resolve("org.assertj:assertj-core")
                .withTransitivity()
                .asFile();
        WebArchive war = ShrinkWrap.create(WebArchive.class, "test.war")
                .addAsLibraries(extraJars)
                .addClass(BroadcasterResource.class)
                .addClass(RestActivator.class)
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
    }

    @AfterEach
    public void after() {
        client.close();
    }

    @Test
    @RunAsClient
    public void testSendMessages() throws Exception {
        var target = client.target(URI.create(baseUrl.toExternalForm() + "api/broadcast"));
        AtomicReference<String> eventData = new AtomicReference<>();
        var latch = new CountDownLatch(1);
        try (SseEventSource eventSource = SseEventSource.target(target).build()) {

            // register event handler
            eventSource.register(inboundSseEvent -> {
                        var data = inboundSseEvent.readData();
                        LOGGER.log(Level.INFO, "received event data: {0}", new Object[]{data});
                        eventData.set(data);
                        latch.countDown();
                    },
                    Throwable::printStackTrace);

            // Subscribe to the event stream.
            eventSource.open();

            // send a simple message
            try (Response r = target.request().post(Entity.text("hello"))) {
                LOGGER.log(Level.INFO, "Get messages response status: {0}", r.getStatus());
                assertEquals(202, r.getStatus());
            }

            latch.await(1000, TimeUnit.MILLISECONDS);

            LOGGER.log(Level.INFO, "message received from SSE broadcaster endpoint: {0}", eventData.get());
            assertThat(eventData.get()).isNotNull();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}