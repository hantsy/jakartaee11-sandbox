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

import com.example.MultipleHttpAuthenticationMechanismHandler;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.HttpHeaders;
import org.glassfish.jersey.client.ClientProperties;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
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
        final String WEBAPP_SRC = "src/main/webapp";
        WebArchive webArchive = ShrinkWrap.create(WebArchive.class)
                .addPackage(MultipleHttpAuthenticationMechanismHandler.class.getPackage())
                .addAsManifestResource("test-persistence.xml", "persistence.xml")
                .addAsWebInfResource("test-beans.xml", "beans.xml")
                .addAsWebInfResource("test-web.xml", "web.xml")
                .addAsWebInfResource("test-faces-config.xml", "faces-config.xml")
                .addAsWebResource(new File(WEBAPP_SRC, "login.xhtml"))
                .addAsWebResource(new File(WEBAPP_SRC, "profile.xhtml"));

        LOGGER.log(Level.INFO, "deployment archive: {0}", webArchive.toString(true));
        return webArchive;
    }

    @ArquillianResource
    private URL baseUrl;

    private Client client;

    @BeforeEach
    public void setup() {
        LOGGER.log(Level.INFO, "deployment baseURL: {0}", baseUrl);
        this.client = ClientBuilder.newBuilder()
                .property(ClientProperties.FOLLOW_REDIRECTS, false)
                .build();
    }

    @Test
    @RunAsClient
    public void testServletPathWithoutAuthInfo() throws Exception {
        URI uri = URI.create(baseUrl.toExternalForm() + "test-servlet");
        LOGGER.log(Level.INFO, "testServletPathWithoutAuthInfo url: {0}", uri);
        var target = client.target(uri);
        try (var response = target.request().get()) {
            LOGGER.info("testServletPathWithoutAuthInfo status: " + response.getStatus());
            // when enabling Redirect.
            Assertions.assertEquals(302, response.getStatus());
        }
    }

    @Test
    @RunAsClient
    public void testRestApiWithAuth() throws Exception {
        URI uri = URI.create(baseUrl.toExternalForm() + "api/hello");
        LOGGER.log(Level.INFO, "testRestApiWithAuth url: {0}", uri);
        var target = client.target(uri);
        try (var response = target.request()
                .header(HttpHeaders.AUTHORIZATION, "Basic " + new String(Base64.getEncoder().encode("restuser:password".getBytes(StandardCharsets.UTF_8))))
                .get()) {
            LOGGER.info("testRestApiWithAuth status: " + response.getStatus());
            Assertions.assertEquals(200, response.getStatus());
        }
    }

    @Test
    @RunAsClient
    public void testRestApiWithWrongRoles() throws Exception {
        URI uri = URI.create(baseUrl.toExternalForm() + "api/hello");
        LOGGER.log(Level.INFO, "testRestApiWithWrongRoles url: {0}", uri);
        var target = client.target(uri);
        try (var response = target.request()
                .header(HttpHeaders.AUTHORIZATION, "Basic " + new String(Base64.getEncoder().encode("webuser:password".getBytes(StandardCharsets.UTF_8))))
                .get()) {
            LOGGER.info("testRestApiWithWrongRoles status: " + response.getStatus());
            Assertions.assertEquals(403, response.getStatus());
        }
    }
}
