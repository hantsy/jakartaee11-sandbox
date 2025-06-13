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
    ContextService defaultContextService;

    @Inject
    ManagedExecutorService defaultExecutorService;

    @Inject
    ManagedThreadFactory defaultThreadFactory;

    @Inject
    ManagedScheduledExecutorService defaultScheduledExecutorService;

    @Resource(lookup = "java:comp/DefaultManagedExecutorService")
    ManagedExecutorService defaultExecutorServiceResource;

    @Resource(lookup = "java:comp/DefaultManagedThreadFactory")
    ManagedThreadFactory defaultThreadFactoryResource;

    @Resource(lookup = "java:comp/DefaultContextService")
    ContextService defaultContextServiceResource;

    @Resource(lookup = "java:comp/DefaultManagedScheduledExecutorService")
    ManagedScheduledExecutorService defaultScheduleExecutorServiceResource;

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

    /*
    @Inject
    @Named("java:comp/cdiExecutor")
    ManagedExecutorService cdiExecutorServiceNamed;

    @Test
    public void testCdiExecutorServiceNamedExistence() {
        assertThat(cdiExecutorServiceNamed).isNotNull();
    }
    */

    @Resource(lookup = "java:comp/cdiExecutor")
    ManagedExecutorService cdiExecutorServiceResource;

    @Resource(lookup = "java:comp/cdiThreadFactory")
    ManagedThreadFactory cdiThreadFactoryResource;

    @Resource(lookup = "java:comp/cdiContextService")
    ContextService cdiContextServiceResource;

    @Resource(lookup = "java:comp/cdiScheduleExecutor")
    ManagedScheduledExecutorService cdiScheduleExecutorServiceResource;

    @Test
    public void testResourceExistence_InjectDefault() {
        assertThat(defaultContextService).isNotNull();
        assertThat(defaultExecutorService).isNotNull();
        assertThat(defaultThreadFactory).isNotNull();
        assertThat(defaultScheduledExecutorService).isNotNull();
    }

    @Test
    public void testResourceExistence_InjectDefaultByResource() {
        assertThat(defaultExecutorServiceResource).isNotNull();
        assertThat(defaultThreadFactoryResource).isNotNull();
        assertThat(defaultContextServiceResource).isNotNull();
        assertThat(defaultScheduleExecutorServiceResource).isNotNull();
    }

    @Test
    public void testExistence_InjectedByResource() {
        assertThat(cdiExecutorServiceResource).isNotNull();
        assertThat(cdiThreadFactoryResource).isNotNull();
        assertThat(cdiContextServiceResource).isNotNull();
        assertThat(cdiScheduleExecutorServiceResource).isNotNull();
    }

    @Test
    public void testResourceExistence_InjectedByQualifier() {
        assertThat(cdiExecutorService).isNotNull();
        assertThat(cdiThreadFactory).isNotNull();
        assertThat(cdiContextService).isNotNull();
        assertThat(cdiScheduleExecutorService).isNotNull();
    }

}
