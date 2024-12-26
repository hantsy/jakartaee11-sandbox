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

import com.example.AsyncConfig;
import com.example.MyQualifier;
import com.example.RestActivator;
import com.example.schedule.StandUpMeeting;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ArquillianExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ScheduleTest {

    private final static Logger LOGGER = Logger.getLogger(ScheduleTest.class.getName());

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        File[] extraJars = Maven
                .resolver()
                .loadPomFromFile("pom.xml")
                .resolve("org.assertj:assertj-core")
                .withTransitivity()
                .asFile();
        var war = ShrinkWrap.create(WebArchive.class, "test.war")
                .addAsLibraries(extraJars)
                .addPackage(StandUpMeeting.class.getPackage())
                .addClasses(RestActivator.class)
                .addClasses(AsyncConfig.class, MyQualifier.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        LOGGER.log(Level.INFO, "war deployment: {0}", new Object[]{war.toString(true)});
        return war;
    }

    @ArquillianResource
    private URL baseUrl;

    Client client;

    @BeforeEach
    public void before() {
        LOGGER.log(Level.INFO, "before running tests.");
        LOGGER.log(Level.INFO, "baseURL: {0}", new Object[]{baseUrl.toExternalForm()});
        client = ClientBuilder.newClient();
    }

    @AfterEach
    public void after() {
        LOGGER.log(Level.INFO, "after running tests.");
    }

    @Test
    @RunAsClient
    @Order(1)
    public void testStartSchedule() throws Exception {
        var target = client.target(URI.create(baseUrl.toExternalForm() + "api/invites"));
        try (Response r = target.request().accept(MediaType.APPLICATION_JSON_TYPE).post(null)) {
            LOGGER.log(Level.INFO, "sending invites status: {0}", r.getStatus());
            assertEquals(200, r.getStatus());
        }
    }

    @Test
    @RunAsClient
    @Order(2)
    public void testInvitedNames() throws Exception {
        Thread.sleep(6_000);
        var target = client.target(URI.create(baseUrl.toExternalForm() + "api/invites"));
        try (Response r = target.request().accept(MediaType.APPLICATION_JSON_TYPE).get()) {
            LOGGER.log(Level.INFO, "get invited names status: {0}", r.getStatus());
            assertEquals(200, r.getStatus());
            var names = r.readEntity(new GenericType<List<String>>() {
            });
            LOGGER.log(Level.INFO, "invited names: {0}", names);
            assertThat(names).isNotEmpty();
        }
    }
}