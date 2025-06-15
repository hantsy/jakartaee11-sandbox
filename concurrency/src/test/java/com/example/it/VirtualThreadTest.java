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
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    ManagedExecutorService executorService;

    @Inject
    ManagedThreadFactory threadFactory;

    @Inject
    ManagedScheduledExecutorService scheduledExecutorService;

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
    void testExecutor() {
        executorService.execute(() -> {
            LOGGER.log(Level.INFO, "current thread name: {0}", new Object[]{Thread.currentThread().getName()});
        });

        vtExecutorService.execute(() -> {
            LOGGER.log(Level.INFO, "current thread name of vtExecutorService: {0}", new Object[]{Thread.currentThread().getName()});
        });

//        [2025-03-20T17:51:55.651097+08:00] [GF 8.0.0-M10] [INFO] [] [com.example.it.VirtualThreadTest] [tid: _ThreadID=147 _ThreadName=concurrent/__defaultManagedExecutorService-ManagedThreadFactory-Thread-1] [levelValue: 800] [[
//        current thread name: concurrent/__defaultManagedExecutorService-ManagedThreadFactory-Thread-1]]
//
//[2025-03-20T17:51:55.657913+08:00] [GF 8.0.0-M10] [INFO] [] [com.example.it.VirtualThreadTest] [tid: _ThreadID=148 _ThreadName=java:comp/vtExecutor-ManagedThreadFactory-Thread-1] [levelValue: 800] [[
//        current thread name on Virtual Thread: java:comp/vtExecutor-ManagedThreadFactory-Thread-1]]
    }

    @Test
    void testScheduledExecutor() {
        var handle = scheduledExecutorService.schedule(() -> {
            LOGGER.log(Level.INFO, "current thread name: {0}", new Object[]{Thread.currentThread().getName()});
        }, 1_000, TimeUnit.MILLISECONDS);

        Runnable canceller = () -> handle.cancel(false);
        scheduledExecutorService.schedule(canceller, 5_000, TimeUnit.MILLISECONDS);

        var vthandle = vtScheduleExecutorService.schedule(() -> {
            LOGGER.log(Level.INFO, "current thread name of vtScheduleExecutorService: {0}", new Object[]{Thread.currentThread().getName()});
        }, 1_000, TimeUnit.MILLISECONDS);

        Runnable vtcanceller = () -> vthandle.cancel(false);
        vtScheduleExecutorService.schedule(vtcanceller, 5_000, TimeUnit.MILLISECONDS);
    }

    @Test
    void testThreadFactory() {
        Thread pThread = threadFactory.newThread(Thread.ofPlatform().name("pThread", 1_000).start(() -> {
            LOGGER.log(Level.INFO, "current thread name: {0}", new Object[]{Thread.currentThread().getName()});
        }));
        pThread.start();

        try {
            Thread.sleep(5_000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        pThread.interrupt();

        Thread vtThread = vtThreadFactory.newThread(Thread.ofVirtual().name("testVt", 1_000).start(() -> {
            LOGGER.log(Level.INFO, "current thread name of vtThreadFactory: {0}", new Object[]{Thread.currentThread().getName()});
        }));
        vtThread.start();
        try {
            Thread.sleep(5_000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        vtThread.interrupt();
    }

    @Test
    void testContextService() {
        var runnable = contextService.contextualRunnable(() -> {
            LOGGER.log(Level.INFO, "current thread name: {0}", new Object[]{Thread.currentThread().getName()});
        });

        var runnableProxy = contextService.createContextualProxy(runnable, Runnable.class);
        try {
            executorService.submit(runnableProxy).get(1_000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }

        var vtrunnable = vtContextService.contextualRunnable(() -> {
            LOGGER.log(Level.INFO, "current thread name of vtContextService: {0}", new Object[]{Thread.currentThread().getName()});
        });

        var vtrunnableProxy = vtContextService.createContextualProxy(vtrunnable, Runnable.class);
        try {
            vtExecutorService.submit(vtrunnableProxy).get(1_000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }
}
