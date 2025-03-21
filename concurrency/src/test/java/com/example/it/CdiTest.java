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

import com.example.cdi.CdiAsyncConfig;
import com.example.cdi.CustomQualifier;
import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ContextService;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.concurrent.ManagedThreadFactory;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ArquillianExtension.class)
public class CdiTest {

    private final static Logger LOGGER = Logger.getLogger(CdiTest.class.getName());

    @Deployment
    public static WebArchive createDeployment() {
        File[] extraJars = Maven
                .resolver()
                .loadPomFromFile("pom.xml")
                .resolve("org.assertj:assertj-core")
                .withTransitivity()
                .asFile();
        var war = ShrinkWrap.create(WebArchive.class, "test.war")
                .addAsLibraries(extraJars)
                .addClasses(CdiAsyncConfig.class, CustomQualifier.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        LOGGER.log(Level.INFO, "war deployment: {0}", new Object[]{war.toString(true)});
        return war;
    }


    @BeforeEach
    public void before() {
        LOGGER.log(Level.INFO, "before running tests.");
    }

    @AfterEach
    public void after() {
        LOGGER.log(Level.INFO, "after running tests.");
    }

    @Inject
    ContextService contextService;

    @Inject
    ManagedExecutorService managedExecutorService;

    @Inject
    ManagedThreadFactory managedThreadFactory;

    @Inject
    ManagedScheduledExecutorService managedScheduledExecutorService;

    @Inject
    @CustomQualifier
    ManagedExecutorService cdiExecutorService;

    @Inject
    @CustomQualifier
    ManagedThreadFactory cdiThreadFactory;

    @Inject
    @CustomQualifier
    ContextService cdiContextService;

    @Inject
    @CustomQualifier
    ManagedScheduledExecutorService cdiScheduleExecutorService;

//    @Inject
//    @Named("java:comp/cdiExecutor")
//    ManagedExecutorService cdiExecutorServiceNamed;

    @Resource(lookup = "java:comp/cdiExecutor")
    ManagedExecutorService cdiExecutorServiceResource;

//    @Test
//    public void testCdiExecutorServiceNamedExistence() {
//        assertThat(cdiExecutorServiceNamed).isNotNull();
//    }

    @Test
    public void testCdiExecutorServiceResourceExistence() {
      assertThat(cdiExecutorServiceResource).isNotNull();
    }

    @Test
    public void testContextServiceExistence() {
        assertThat(contextService).isNotNull();
    }

    @Test
    public void testManagedExecutorServiceExistence() {
        assertThat(managedExecutorService).isNotNull();
    }

    @Test
    public void testManagedThreadFactoryExistence() {
        assertThat(managedThreadFactory).isNotNull();
    }

    @Test
    public void testManagedScheduledExecutorServiceExistence() {
        assertThat(managedScheduledExecutorService).isNotNull();
    }

    @Test
    public void testCustomExecutorServiceExistence() {
        assertThat(cdiExecutorService).isNotNull();
    }

    @Test
    public void testCustomThreadFactoryExistence() {
        assertThat(cdiThreadFactory).isNotNull();
    }

    @Test
    public void testCustomContextServiceExistence() {
        assertThat(cdiContextService).isNotNull();
    }

    @Test
    public void testCustomScheduledExecutorServiceExistence() {
        assertThat(cdiScheduleExecutorService).isNotNull();
    }
}
