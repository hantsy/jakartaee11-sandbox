# What's New in Jakarta REST 4.0

Jakarta REST 4.0 is a major update in Jakarta EE 11, with much of the work focused on housekeeping. For example, there has been significant effort to modernize the Jakarta REST TCK. Additionally, support for the `ManagedBean` and `JAXB` specifications has been removed.

For developers, there are a few notable API changes:

* New convenient methods for checking a header value, especially which contains a token-separated list, including [`HttpHeaders#containsHeaderString`](https://jakarta.ee/specifications/restful-ws/4.0/apidocs/jakarta.ws.rs/jakarta/ws/rs/core/httpheaders#containsHeaderString(java.lang.String,java.util.function.Predicate)), [`ClientRequestContext#containsHeaderString`](https://jakarta.ee/specifications/restful-ws/4.0/apidocs/jakarta.ws.rs/jakarta/ws/rs/client/clientrequestcontext#containsHeaderString(java.lang.String,java.util.function.Predicate)), [`ClientResponseContext#containsHeaderString`](https://jakarta.ee/specifications/restful-ws/4.0/apidocs/jakarta.ws.rs/jakarta/ws/rs/client/clientresponsecontext#containsHeaderString(java.lang.String,java.util.function.Predicate)), [`ContainerRequestContext#containsHeaderString`](https://jakarta.ee/specifications/restful-ws/4.0/apidocs/jakarta.ws.rs/jakarta/ws/rs/container/containerrequestcontext#containsHeaderString(java.lang.String,java.util.function.Predicate)), and [`ContainerResponseContext#containsHeaderString`](https://jakarta.ee/specifications/restful-ws/4.0/apidocs/jakarta.ws.rs/jakarta/ws/rs/container/containerresponsecontext#containsHeaderString(java.lang.String,java.util.function.Predicate)).
* A new method, [`UriInfo#getMatchedResourceTemplate`](https://jakarta.ee/specifications/restful-ws/4.0/apidocs/jakarta.ws.rs/jakarta/ws/rs/core/uriinfo#getMatchedResourceTemplate()), to retrieve the URI template for all paths of the current request.
* Added support for JSON Merge Patch.

The first two are minor improvements. Let's take a closer look at JSON Merge Patch.

## An Introduction to JSON Merge Patch

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

## Example Project

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

For comparison, we also include two JSON Patch (defined by [RFC 6902](https://datatracker.ietf.org/doc/html/rfc6902) and implemented in Java EE 8/JAX-RS 2.1) example endpoints: one for processing an array of operations, and another for handling a single resource entity.

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

In the above test:

* The deployment is marked as *testable*, meaning the test runs as a client and interacts with the service deployed in the test archive.
* After deployment, the `@ArquillianResource`-annotated `URL` provides the application's base URL, including the `ApplicationPath` defined in the `Application` class, ending with a `/`.
* We set `.property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)` to ensure the custom `PATCH` method works correctly with the current Jakarta REST Client API.

Let's focus on the `testGetArticleByIdAndMergePatch` test method, which demonstrates the JSON Merge Patch functionality:

* First, retrieve the resource.
* Modify it and use `Json.createMergeDiff` to create a patch `JsonObject`.
* Apply the patch to the remote resource.
* Finally, retrieve the resource again to verify that the patch was applied successfully.

> [!WARNING]
> The Jakarta REST Client API does not provide a `patch()` method, similar to the existing `get()` or `post()`. See the related discussion: [jakartaee/rest#1276](https://github.com/jakartaee/rest/issues/1276).

Get the [complete example project](https://github.com/hantsy/jakartaee11-sandbox/tree/master/rest) from my GitHub repository. 

## Final Thoughts 

Over the past decade, I have developed many backend RESTful API applications. However, I have noticed a growing trend: more customers are choosing Spring WebMvc or WebFlux as their preferred frameworks over Jakarta REST. While libraries and frameworks like RESTEasy and Quarkus help fill some gaps, Jakarta REST itself has evolved slowly. Features like JSON Patch and the new JSON Merge Patch introduced in this version are rarely used in real-world RESTful API development. Even Spring once incubated a project called **Spring Sync** to address similar needs, but it has since been abandoned.

In my view, since version 2.1, Jakarta REST has not delivered significant features that boost developer productivity. The following is my wishlist for the next generation of Jakarta REST.

* Deprecating `Resource/Context` injection in favor of CDI `@Inject` ([jakartaee/rest#951](https://github.com/jakartaee/rest/issues/951), [jakartaee/rest#569](https://github.com/jakartaee/rest/issues/569)), and replacing `@Provider` with CDI `@Produces` or programmatic configuration in the `Application` class.
* Supporting async/reactive return types natively, as has been available in Quarkus for years, and moving `@Suspended AsyncResponse` handling to background concurrency and context propagation ([jakartaee/rest#1281](https://github.com/jakartaee/rest/issues/1281)).
* Providing default values for query, form, and path parameter names ([jakartaee/rest#579](https://github.com/jakartaee/rest/issues/579)).
* Adding support for Problem Details ([jakartaee/rest#1150](https://github.com/jakartaee/rest/issues/1150)).
* Adding support for API Versioning ([jakartaee/rest#1317](https://github.com/jakartaee/rest/issues/1317)).
* Adding support for Hypermedia, eg, HAL, HAL Form, etc. ([jakartaee/rest#1323](https://github.com/jakartaee/rest/issues/1323)).
* Supporting Java records in FormBeans and related areas ([jakartaee/rest#955](https://github.com/jakartaee/rest/issues/955), [jakartaee/rest#913](https://github.com/jakartaee/rest/issues/913)), especially since records are a major feature in EE 11.
* Enabling functional programming styles for both client and server code ([jakartaee/rest#1301](https://github.com/jakartaee/rest/issues/1301)).
* Defining HTTP service interfaces as contracts between client and server ([jakartaee/rest#1294](https://github.com/jakartaee/rest/issues/1294)).
* Modernizing the client API to use Java 8+ syntax and making the HTTP client engine easily switchable ([jakartaee/rest#1282](https://github.com/jakartaee/rest/issues/1282)).
* ...

I hope the Jakarta REST expert group will focus more on features that improve developer productivity and address real-world needs.
