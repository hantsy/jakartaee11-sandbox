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
import jakarta.json.Json;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ArquillianExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ArticleResourceTest {

    private final static Logger LOGGER = Logger.getLogger(ArticleResourceTest.class.getName());

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        File[] extraJars = Maven
                .resolver()
                .loadPomFromFile("pom.xml")
                .importCompileAndRuntimeDependencies()
                .resolve("org.assertj:assertj-core")
                .withTransitivity()
                .asFile();
        var war = ShrinkWrap.create(WebArchive.class, "test.war")
                .addAsLibraries(extraJars)
                .addClasses(
                        ArticleResource.class,
                        Article.class,
                        Author.class,
                        ArticleRepository.class,

                        // jaxrs config
                        JsonbContextResolver.class,
                        RestActivator.class
                )
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        LOGGER.log(Level.INFO, "war deployment: {0}", new Object[]{war.toString(true)});
        return war;
    }

    @ArquillianResource
    private URL baseUrl;

    Client client;

    private Jsonb jsonb = JsonbBuilder.create();

    @BeforeEach
    public void before() {
        LOGGER.log(Level.INFO, "baseURL: {0}", new Object[]{baseUrl.toExternalForm()});
        client = ClientBuilder.newClient();
        client.register(JsonbContextResolver.class);
    }

    @AfterEach
    public void after() {
        client.close();
    }

    @Test
    @RunAsClient
    @Order(1)
    public void testGetArticles() {
        var target = client.target(URI.create(baseUrl.toExternalForm() + "api/articles"));
        List<Article> articleList;
        try (Response r = target.request().accept(MediaType.APPLICATION_JSON_TYPE).get()) {
            LOGGER.log(Level.INFO, "Get response status: {0}", r.getStatus());
            assertEquals(200, r.getStatus());
            articleList = r.readEntity(new GenericType<>() {
            });

            LOGGER.log(Level.INFO, "all articles: {0}", articleList);
            assertThat(articleList.size()).isEqualTo(2);
        }

        // apply json patch
        var patch = Json.createPatchBuilder()
                .replace("/1/content", "Updated by JsonPatch")
                .remove("/1/author/familyName")
                .add("/1/tags/1", "JAX-RS")
                // add root node is an object
                //.add("/", Json.createObjectBuilder().add("title", "My new article").build())
                .build().toJsonArray();

        //see: https://stackoverflow.com/questions/22355235/patch-request-using-jersey-client
        var target2 = client
                .property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
                .target(URI.create(baseUrl.toExternalForm() + "api/articles"));
        try (Response r2 = target2
                .request()
                //.header("Content-Type", MediaType.APPLICATION_JSON_PATCH_JSON)
                .method("PATCH", Entity.entity(patch, MediaType.APPLICATION_JSON_PATCH_JSON_TYPE))) {
            LOGGER.log(Level.INFO, "patch response status: {0}", r2.getStatus());
            assertEquals(204, r2.getStatus());
        }

        // verify the patched result
        try (Response r = target.request().accept(MediaType.APPLICATION_JSON_TYPE).get()) {
            LOGGER.log(Level.INFO, "Get response status after applying patch: {0}", r.getStatus());
            assertEquals(200, r.getStatus());
            articleList = r.readEntity(new GenericType<>() {
            });

            LOGGER.log(Level.INFO, "all articles after applying patch: {0}", articleList);
            assertThat(articleList.size()).isEqualTo(2);
        }
    }

    @Test
    @RunAsClient
    @Order(2)
    public void testGetArticleByid() {
        var target = client.target(URI.create(baseUrl.toExternalForm() + "api/articles/1"));
        try (Response r = target.request().accept(MediaType.APPLICATION_JSON_TYPE).get()) {
            LOGGER.log(Level.INFO, "Get response status: {0}", r.getStatus());
            assertEquals(200, r.getStatus());
            Article article = r.readEntity(Article.class);

            LOGGER.log(Level.INFO, "get article by id: {0}", article);
            assertThat(article.title()).isEqualTo("My first article");
        }

        var patch = Json.createPatchBuilder()
                .replace("/title", "My title updated by JsonPatch")
                .build().toJsonArray();

//        var patch = Json.createDiff(source, target).toJsonArray();

        var target2 = client
                .property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
                .target(URI.create(baseUrl.toExternalForm() + "api/articles/1"));
        try (Response r2 = target2
                .request()
                .method("PATCH", Entity.entity(patch, MediaType.APPLICATION_JSON_PATCH_JSON_TYPE))) {
            LOGGER.log(Level.INFO, "patch response status: {0}", r2.getStatus());
            assertEquals(204, r2.getStatus());
        }

        // verify the patched result
        try (Response r = target.request().accept(MediaType.APPLICATION_JSON_TYPE).get()) {
            LOGGER.log(Level.INFO, "Get response status after applying patch: {0}", r.getStatus());
            assertEquals(200, r.getStatus());
            Article article = r.readEntity(Article.class);

            LOGGER.log(Level.INFO, "get article by id after applying patch: {0}", article);
            assertThat(article.title()).isEqualTo("My title updated by JsonPatch");
        }
    }

    @Test
    @RunAsClient
    @Order(3)
    public void testGetArticleByIdAndMergePatch() {
        var target = client.target(URI.create(baseUrl.toExternalForm() + "api/articles/2"));
        Article article = null;
        try (Response r = target.request().accept(MediaType.APPLICATION_JSON_TYPE).get()) {
            LOGGER.log(Level.INFO, "Get response status: {0}", r.getStatus());
            assertEquals(200, r.getStatus());
            article = r.readEntity(Article.class);

            LOGGER.log(Level.INFO, "get article by id: {0}", article);
            assertThat(article.title()).isEqualTo("My second article");
        }

        var updated = article.withTags(List.of("JAX-RS", "RESTEasy", "Jersey"))
                .withAuthor(new Author("Jack", "Ma"));
        var patch =
                Json.createMergeDiff(
                        Json.createReader(new StringReader(jsonb.toJson(article))).readObject(),
                        Json.createReader(new StringReader(jsonb.toJson(updated))).readObject()
                )
                .toJsonValue();

        var target2 = client
                .property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
                .target(URI.create(baseUrl.toExternalForm() + "api/articles/2"));
        try (Response r2 = target2
                .request()
                .method("PATCH", Entity.entity(patch, "application/merge-patch+json"))) {
            LOGGER.log(Level.INFO, "patch response status: {0}", r2.getStatus());
            assertEquals(204, r2.getStatus());
        }

        // verify the patched result
        try (Response r = target.request().accept(MediaType.APPLICATION_JSON_TYPE).get()) {
            LOGGER.log(Level.INFO, "Get response status after applying patch: {0}", r.getStatus());
            assertEquals(200, r.getStatus());
            article = r.readEntity(Article.class);

            LOGGER.log(Level.INFO, "get article by id after applying patch: {0}", article);
            assertThat(article.title()).isEqualTo("My second article");
            assertThat(article.tags()).isEqualTo(List.of("JAX-RS", "RESTEasy", "Jersey"));
            assertThat(article.author()).isEqualTo(new Author("Jack", "Ma"));
        }

    }
}