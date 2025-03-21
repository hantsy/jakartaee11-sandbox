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

import com.example.vt.VirtualThreadAsyncConfig;
import com.example.vt.WithVirtualThread;
import jakarta.enterprise.concurrent.ContextService;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.concurrent.ManagedThreadFactory;
import jakarta.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ArquillianExtension.class)
public class VirtualThreadTest {

    private final static Logger LOGGER = Logger.getLogger(VirtualThreadTest.class.getName());

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
                .addClasses(VirtualThreadAsyncConfig.class, WithVirtualThread.class)
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
    @WithVirtualThread
    ManagedExecutorService vtExecutorService;

    @Inject
    @WithVirtualThread
    ManagedThreadFactory vtThreadFactory;

    @Inject
    @WithVirtualThread
    ContextService vtContextService;

    @Inject
    @WithVirtualThread
    ManagedScheduledExecutorService vtScheduleExecutorService;


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
    public void testVirtualExecutorServiceExistence() {
        assertThat(vtExecutorService).isNotNull();
    }

    @Test
    public void testVirtualThreadFactoryExistence() {
        assertThat(vtThreadFactory).isNotNull();
    }

    @Test
    public void testVirtualContextServiceExistence() {
        assertThat(vtContextService).isNotNull();
    }

    @Test
    public void testVirtualScheduledExecutorServiceExistence() {
        assertThat(vtScheduleExecutorService).isNotNull();
    }

    @Test
    public void testVirtualThreadName() {
        managedExecutorService.execute(() -> {
            LOGGER.log(Level.INFO, "current thread name: {0}", new Object[]{Thread.currentThread().getName()});
        });

        vtExecutorService.execute(() -> {
            LOGGER.log(Level.INFO, "current thread name on Virtual Thread: {0}", new Object[]{Thread.currentThread().getName()});
        });

//        [2025-03-20T17:51:55.651097+08:00] [GF 8.0.0-M10] [INFO] [] [com.example.it.VirtualThreadTest] [tid: _ThreadID=147 _ThreadName=concurrent/__defaultManagedExecutorService-ManagedThreadFactory-Thread-1] [levelValue: 800] [[
//        current thread name: concurrent/__defaultManagedExecutorService-ManagedThreadFactory-Thread-1]]
//
//[2025-03-20T17:51:55.657913+08:00] [GF 8.0.0-M10] [INFO] [] [com.example.it.VirtualThreadTest] [tid: _ThreadID=148 _ThreadName=java:comp/vtExecutor-ManagedThreadFactory-Thread-1] [levelValue: 800] [[
//        current thread name on Virtual Thread: java:comp/vtExecutor-ManagedThreadFactory-Thread-1]]
    }
}
