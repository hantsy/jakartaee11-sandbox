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

import com.example.ApplicationProperties;
import com.example.RestActivator;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(ArquillianExtension.class)
public class ApplicationTest {

    private final static Logger LOGGER = Logger.getLogger(ApplicationTest.class.getName());

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        File[] extraJars = Maven
                .resolver()
                .loadPomFromFile("pom.xml")
                .importCompileAndRuntimeDependencies()
                .resolve("org.assertj:assertj-core")
                .withTransitivity()
                .asFile();
        WebArchive war = ShrinkWrap.create(WebArchive.class)
                .addAsLibraries(extraJars)
                .addPackage(RestActivator.class.getPackage())
                .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        LOGGER.log(Level.INFO, war.toString(true));
        return war;
    }

    @ArquillianResource
    URL baseUrl;

    Client client;

    @BeforeEach
    public void before() {
        LOGGER.log(Level.INFO, "baseURL: {0}", new Object[]{baseUrl.toExternalForm()});
        client = ClientBuilder.newClient();
        //client.register(JsonbContextResolver.class);
    }

    @AfterEach
    public void after() throws Exception {
        LOGGER.log(Level.INFO, "base url: {0}", baseUrl);
    }

    @Test
    @RunAsClient
    public void testApplicationInfo() {
        var target = client.target(URI.create(baseUrl.toExternalForm() + "api/app/info"));
        ApplicationProperties appInfo;
        try (Response r = target.request().accept(MediaType.APPLICATION_JSON_TYPE).get()) {
            LOGGER.log(Level.INFO, "Get application info status: {0}", r.getStatus());
            assertEquals(200, r.getStatus());
            appInfo = r.readEntity(ApplicationProperties.class);
        }
        LOGGER.log(Level.INFO, "Get application info result: {0}", appInfo);
        assertThat(appInfo).isNotNull();
        assertThat(appInfo.name()).isEqualTo("test name");
        assertThat(appInfo.description()).isEqualTo("test description");
    }
}
