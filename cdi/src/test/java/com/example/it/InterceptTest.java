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

import com.example.intercept.Greeter;
import jakarta.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

@ExtendWith(ArquillianExtension.class)
public class InterceptTest {

    private final static Logger LOGGER = Logger.getLogger(InterceptTest.class.getName());

    @Deployment()
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
                .addPackage(Greeter.class.getPackage())
                .addAsWebInfResource("test-intercept-beans.xml", "beans.xml");
        LOGGER.log(Level.INFO, war.toString(true));
        return war;
    }

    @Inject
    Greeter greeter;

    @Test
    public void testApplicationInfo() throws Exception {
        IntStream.rangeClosed(1, 5)
                .forEachOrdered(value -> {
                    LOGGER.info("This is the " + value + " calls:");
                    try {
                        greeter.sayHello("Jakarta EE 11 at the #" + value + " times");
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
