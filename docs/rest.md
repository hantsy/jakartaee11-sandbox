# What's New in Jakarta REST 4.0

Jakarta REST 4.0 is a major update in Jakarta EE 11, with much of the work focused on housekeeping. For example, there has been significant effort to modernize the Jakarta REST TCK. Additionally, support for the `ManagedBean` and `JAXB` specifications has been removed.

For developers, there are a few notable API changes:
* New convenient methods for checking whether a header value contains a token-separated list, including [`HttpHeaders#containsHeaderString`](https://jakarta.ee/specifications/restful-ws/4.0/apidocs/jakarta.ws.rs/jakarta/ws/rs/core/httpheaders#containsHeaderString(java.lang.String,java.util.function.Predicate)), [`ClientRequestContext#containsHeaderString`](https://jakarta.ee/specifications/restful-ws/4.0/apidocs/jakarta.ws.rs/jakarta/ws/rs/client/clientrequestcontext#containsHeaderString(java.lang.String,java.util.function.Predicate)), [`ClientResponseContext#containsHeaderString`](https://jakarta.ee/specifications/restful-ws/4.0/apidocs/jakarta.ws.rs/jakarta/ws/rs/client/clientresponsecontext#containsHeaderString(java.lang.String,java.util.function.Predicate)), [`ContainerRequestContext#containsHeaderString`](https://jakarta.ee/specifications/restful-ws/4.0/apidocs/jakarta.ws.rs/jakarta/ws/rs/container/containerrequestcontext#containsHeaderString(java.lang.String,java.util.function.Predicate)), and [`ContainerResponseContext#containsHeaderString`](https://jakarta.ee/specifications/restful-ws/4.0/apidocs/jakarta.ws.rs/jakarta/ws/rs/container/containerresponsecontext#containsHeaderString(java.lang.String,java.util.function.Predicate)).
* A new method, [`UriInfo#getMatchedResourceTemplate`](https://jakarta.ee/specifications/restful-ws/4.0/apidocs/jakarta.ws.rs/jakarta/ws/rs/core/uriinfo#getMatchedResourceTemplate()), to retrieve the URI template for all paths of the current request.
* Added support for JSON Merge Patch.

The first two are minor improvements. Let's take a closer look at JSON Merge Patch.

JSON Merge Patch is defined in [RFC 7386](https://datatracker.ietf.org/doc/html/rfc7386) as follows:

> This specification defines the JSON merge patch format and processing rules. The merge patch format is primarily intended for use with the HTTP PATCH method as a means of describing a set of modifications to a target resource's content.

Consider the following example JSON document:

```json
{
    "title": "My second article",
    "author": {
        "givenName": "Hantsy",
        "familyName": "Bai"
    },
    "tags": ["second", "article"],
    "content": "The content of my second article"
}
```

Suppose you want to update the tags to `"JAX-RS", "RESTEasy", "Jersey"` and change the author to `"Jack", "Ma"`. You would send a request like this:

```json
PATCH /articles/2 HTTP/1.1
Host: localhost
Content-Type: application/merge-patch+json

{
    "author": {
        "givenName": "Jack",
        "familyName": "Ma"
    },
    "tags": ["JAX-RS", "RESTEasy", "Jersey"]
}
```

The resulting JSON document would be:

```json
{
    "title": "My second article",
    "author": {
        "givenName": "Jack",
        "familyName": "Ma"
    },
    "tags": ["JAX-RS", "RESTEasy", "Jersey"],
    "content": "The content of my second article"
}
```

Let's walk through a simple REST resource example to demonstrate this process in code.

Assume we need to manage a collection of articles, represented by an `Article` class:

```java
// Article.java
public record Article(
        Integer id,
        String title,
        Author author,
        String content,
        List<String> tags,
        LocalDateTime publishedAt
) {
    public Article withId(int id) {
        return new Article(id, title, author, content, tags, publishedAt);
    }

    public Article withTags(List<String> tags) {
        return new Article(id, title, author, content, tags, publishedAt);
    }

    public Article withAuthor(Author author) {
        return new Article(id, title, author, content, tags, publishedAt);
    }
}

// Author.java
public record Author(String givenName, String familyName) {
}
```

As mentioned in [Java SE Record support in Jakarta EE 11](./record.md), although JSON-B did not fully align with Record support in Jakarta EE 11, Eclipse Yasson already supports serialization and deserialization of records.

The `ArticleRepository` is a simple in-memory repository:

```java
@ApplicationScoped
public class ArticleRepository {
    private static final ConcurrentHashMap<Integer, Article> articles = new ConcurrentHashMap<>();
    private static final AtomicInteger ID_GEN = new AtomicInteger(1);

    static {
        var id1 = ID_GEN.getAndIncrement();
        articles.put(
            id1,
            new Article(id1, "My first article",
                new Author("Hantsy", "Bai"),
                "This is my first article",
                List.of("first", "article"),
                LocalDateTime.now())
        );
        var id2 = ID_GEN.getAndIncrement();
        articles.put(id2,
            new Article(id2, "My second article",
                new Author("Hantsy", "Bai"),
                "This is my second article",
                List.of("second", "article"),
                LocalDateTime.now())
        );
    }

    public List<Article> findAll() {
        return List.copyOf(articles.values());
    }

    public Article findById(int id) {
        return articles.get(id);
    }

    public Article save(Article article) {
        if (article.id() == null) {
            var id = ID_GEN.getAndIncrement();
            article = article.withId(id);
        }
        articles.put(article.id(), article);
        return article;
    }
}
```

Now, let's look at the `ArticleResource`:

```java
@Path("articles")
@RequestScoped
public class ArticleResource {

    @Inject
    ArticleRepository repository;

    Jsonb jsonb;

    @PostConstruct
    public void init() {
        jsonb = JsonbBuilder.create();
    }

    @GET
    public Response getArticles() {
        return Response.ok(repository.findAll()).build();
    }

    @GET
    @Path("{id}")
    public Response getArticle(@PathParam("id") Integer id) {
        return Response.ok(repository.findById(id)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createArticle(Article article) {
        var saved = repository.save(article);
        return Response.created(URI.create("/articles/" + saved.id())).build();
    }

    @PATCH
    @Consumes(MediaType.APPLICATION_JSON_PATCH_JSON)
    public Response saveOrUpdateAllArticles(JsonArray patch) {
        var all = repository.findAll();
        var result = Json.createPatch(patch)
            .apply(Json.createReader(new StringReader(jsonb.toJson(all))).readArray());
        List<Article> articles = jsonb.fromJson(
            jsonb.toJson(result),
            new ArrayList<Article>() {}.getClass().getGenericSuperclass()
        );
        articles.forEach(repository::save);

        return Response.noContent().build();
    }

    @PATCH
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON_PATCH_JSON)
    public Response updateArticle(@PathParam("id") Integer id, JsonArray patch) {
        var target = repository.findById(id);
        var patchedResult = Json.createPatch(patch)
            .apply(Json.createReader(new StringReader(jsonb.toJson(target))).readObject());
        var article = jsonb.fromJson(jsonb.toJson(patchedResult), Article.class);
        repository.save(article);

        return Response.noContent().build();
    }

    @PATCH
    @Path("{id}")
    //@Consumes(MediaType.APPLICATION_MERGE_PATCH_JSON) // added in 4.0
    @Consumes("application/merge-patch+json")
    public Response mergeArticle(@PathParam("id") Integer id, JsonObject patch) {
        var targetArticle = repository.findById(id);
        var mergedResult = Json.createMergePatch(patch)
            .apply(Json.createReader(new StringReader(jsonb.toJson(targetArticle))).readObject());
        var article = jsonb.fromJson(jsonb.toJson(mergedResult), Article.class);
        repository.save(article);

        return Response.noContent().build();
    }
}
```

For comparison, we also include a JSON Patch example (defined by [RFC 6902](https://datatracker.ietf.org/doc/html/rfc6902) and implemented in Java EE 8/JAX-RS 2.1).

Let's create an Arquillian test to verify the functionality:

```java
@ExtendWith(ArquillianExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ArticleResourceTest {

    private static final Logger LOGGER = Logger.getLogger(ArticleResourceTest.class.getName());

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
        LOGGER.log(Level.INFO, "war deployment: {0}", war.toString(true));
        return war;
    }

    @ArquillianResource
    private URL baseUrl;

    Client client;

    private Jsonb jsonb = JsonbBuilder.create();

    @BeforeEach
    public void before() {
        LOGGER.log(Level.INFO, "baseURL: {0}", baseUrl.toExternalForm());
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
            articleList = r.readEntity(new GenericType<>() {});
            LOGGER.log(Level.INFO, "all articles: {0}", articleList);
            assertThat(articleList.size()).isEqualTo(2);
        }

        // Apply JSON Patch
        var patch = Json.createPatchBuilder()
            .replace("/1/content", "Updated by JsonPatch")
            .remove("/1/author/familyName")
            .add("/1/tags/1", "JAX-RS")
            .build().toJsonArray();

        var target2 = client
            .property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
            .target(URI.create(baseUrl.toExternalForm() + "api/articles"));
        try (Response r2 = target2
            .request()
            .method("PATCH", Entity.entity(patch, MediaType.APPLICATION_JSON_PATCH_JSON_TYPE))) {
            LOGGER.log(Level.INFO, "patch response status: {0}", r2.getStatus());
            assertEquals(204, r2.getStatus());
        }

        // Verify the patched result
        try (Response r = target.request().accept(MediaType.APPLICATION_JSON_TYPE).get()) {
            LOGGER.log(Level.INFO, "Get response status after applying patch: {0}", r.getStatus());
            assertEquals(200, r.getStatus());
            articleList = r.readEntity(new GenericType<>() {});
            LOGGER.log(Level.INFO, "all articles after applying patch: {0}", articleList);
            assertThat(articleList.size()).isEqualTo(2);
        }
    }

    @Test
    @RunAsClient
    @Order(2)
    public void testGetArticleById() {
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

        var target2 = client
            .property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
            .target(URI.create(baseUrl.toExternalForm() + "api/articles/1"));
        try (Response r2 = target2
            .request()
            .method("PATCH", Entity.entity(patch, MediaType.APPLICATION_JSON_PATCH_JSON_TYPE))) {
            LOGGER.log(Level.INFO, "patch response status: {0}", r2.getStatus());
            assertEquals(204, r2.getStatus());
        }

        // Verify the patched result
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
        var patch = Json.createMergeDiff(
            Json.createReader(new StringReader(jsonb.toJson(article))).readObject(),
            Json.createReader(new StringReader(jsonb.toJson(updated))).readObject()
        ).toJsonValue();

        var target2 = client
            .property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
            .target(URI.create(baseUrl.toExternalForm() + "api/articles/2"));
        try (Response r2 = target2
            .request()
            .method("PATCH", Entity.entity(patch, "application/merge-patch+json"))) {
            LOGGER.log(Level.INFO, "patch response status: {0}", r2.getStatus());
            assertEquals(204, r2.getStatus());
        }

        // Verify the patched result
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
```

In this test:
* The deployment is marked as *testable*, meaning the test runs as a client and interacts with the service deployed in the test archive.
* After deployment, the `@ArquillianResource`-annotated `URL` provides the application's base URL, including the `ApplicationPath` defined in the `Application` class, ending with a `/`.
* We set `.property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)` to ensure the custom `PATCH` method works correctly with the current Jakarta REST Client API.

Let's focus on the `testGetArticleByIdAndMergePatch` test, which demonstrates the JSON Merge Patch functionality:
* First, retrieve the resource.
* Modify it and use `Json.createMergeDiff` to create a patch `JsonObject`.
* Apply the patch to the remote resource.
* Finally, retrieve the resource again to verify that the patch was applied successfully.

> [!WARNING]
> The Jakarta REST Client API does not provide a `patch()` method, similar to the existing `get()` or `post()`. See the related discussion: [jakartaee/rest#1276](https://github.com/jakartaee/rest/issues/1276).


