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

import com.example.*;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.HttpHeaders;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

@ExtendWith(ArquillianExtension.class)
public class SecurityTest {

    private final static Logger LOGGER = Logger.getLogger(SecurityTest.class.getName());

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive webArchive = ShrinkWrap.create(WebArchive.class)
                .addPackage(MultipleHttpAuthenticationMechanismHandler.class.getPackage())
                .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource("test-web.xml", "web.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        LOGGER.log(Level.INFO, "deployment archive: {0}", webArchive.toString(true));
        return webArchive;
    }

    @ArquillianResource
    private URL baseUrl;

    private Client client;

    @BeforeEach
    public void setup() {
        LOGGER.log(Level.INFO, "deployment baseURL: {0}", baseUrl);
        this.client = ClientBuilder.newClient();
    }

    @Test
    @RunAsClient
    public void testServletPath() throws Exception {
        var target = client.target(URI.create(baseUrl.toExternalForm() + "servlet"));
        try (var response = target.request().get()) {
            LOGGER.info("response status: " + response.getStatus());
            Assertions.assertEquals(401, response.getStatus());
        }
    }

    @Test
    @RunAsClient
    public void testServletPathWithAuth() throws Exception {
        var target = client.target(URI.create(baseUrl.toExternalForm() + "api/hello"));
        try (var response = target.request()
                .header(HttpHeaders.AUTHORIZATION, "Basic " + new String(Base64.getEncoder().encode("restuser:password".getBytes(StandardCharsets.UTF_8))))
                .get()) {
            LOGGER.info("response status: " + response.getStatus());
            Assertions.assertEquals(200, response.getStatus());
        }
    }

    @Test
    @RunAsClient
    public void testServletPathWithWrongRoles() throws Exception {
        var target = client.target(URI.create(baseUrl.toExternalForm() + "api/hello"));
        try (var response = target.request()
                .header(HttpHeaders.AUTHORIZATION, "Basic " + new String(Base64.getEncoder().encode("webuser:password".getBytes(StandardCharsets.UTF_8))))
                .get()) {
            LOGGER.info("response status: " + response.getStatus());
            Assertions.assertEquals(401, response.getStatus());
        }
    }
}
