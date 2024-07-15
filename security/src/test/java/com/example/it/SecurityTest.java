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
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

@ExtendWith(ArquillianExtension.class)
public class SecurityTest {

    private final static Logger LOGGER = Logger.getLogger(SecurityTest.class.getName());

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive webArchive = ShrinkWrap.create(WebArchive.class)
                .addClasses(
                        FacesActivator.class,
                        LoginBean.class,
                        Resources.class,
                        SecurityConfig.class,
                        TestServlet.class
                )
                .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        LOGGER.log(Level.INFO, "deployment archive: {0}", webArchive.toString(true));
        return webArchive;
    }

    @ArquillianResource
    private URL baseUrl;

    private Client client;

    @BeforeEach
    public void setup() {
        this.client = ClientBuilder.newClient();
    }

    @Test
    @RunAsClient
    public void testRecordIdClass() throws Exception {
        var target = client.target(baseUrl.toExternalForm());

        try (var response = target.path("/servlet")
                .request()
                .get()) {

            LOGGER.info("response status: " + response.getStatus());
        }
    }
}
