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
import com.example.schedule.Invite;
import com.example.schedule.StandUpMeeting;
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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ArquillianExtension.class)
public class ScheduleTest {

    private final static Logger LOGGER = Logger.getLogger(ScheduleTest.class.getName());

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
                .addPackage(StandUpMeeting.class.getPackage())
                .addClasses(AsyncConfig.class, MyQualifier.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        LOGGER.log(Level.INFO, "war deployment: {0}", new Object[]{war.toString(true)});
        return war;
    }

    @Inject
    Invite invite;

    @BeforeEach
    public void before() {
        LOGGER.log(Level.INFO, "before running tests.");
    }

    @AfterEach
    public void after() {
        LOGGER.log(Level.INFO, "after running tests.");
    }

    @Test
    public void testSchedule() throws Exception {
        assertThat(invite).isNotNull();
        Thread.sleep(6_000);
        List<String> names = invite.getNames();
        LOGGER.log(Level.INFO, "invites are sent to: {0}", new Object[]{names});
        assertThat(names).isNotEmpty();
    }
}