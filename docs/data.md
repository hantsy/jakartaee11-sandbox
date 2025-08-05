# Meet Jakarta Data: The Newest Member of the Jakarta EE 11 Ecosystem

[Jakarta Data](https://jakarta.ee/specifications/data/1.0/) is a new specification introduced in Jakarta EE 11. Jakarta Data provides a set of APIs to simplify data access, and it is not limited to Jakarta Persistence or relational databases. It is designed to be neutral regarding the underlying data storage. The [Eclipse JNoSQL project](https://www.jnosql.org/) also implements this specification, bringing the same experience to the NoSQL world.

## Exploring Jakarta Data

Similar to [Spring Data Commons](https://github.com/spring-projects/spring-data-commons), [Micronaut Data](https://github.com/micronaut-projects/micronaut-data), and [Quarkus Panache](https://quarkus.io/guides/hibernate-orm-panache), Jakarta Data introduces a `Repository` abstraction. This includes a basic [`DataRepository`](https://jakarta.ee/specifications/data/1.0/apidocs/jakarta.data/jakarta/data/repository/datarepository) interface to indicate a repository, as well as two interfaces, [`BasicRepository`](https://jakarta.ee/specifications/data/1.0/apidocs/jakarta.data/jakarta/data/repository/basicrepository) and [`CrudRepository`](https://jakarta.ee/specifications/data/1.0/apidocs/jakarta.data/jakarta/data/repository/crudrepository), which provide common CRUD operations for underlying data storage. It also introduces a new annotation, [`@Repository`](https://jakarta.ee/specifications/data/1.0/apidocs/jakarta.data/jakarta/data/repository/repository), to mark an interface as a repository, whether it is derived from the common interfaces or is a pure interface that does not extend any existing interface.

For example, the following `PostRepository` interface is for the entity `Post`.

```java
@Repository
public interface PostRepository extends CrudRepository<Post, UUID> {

}
```

In addition, Jakarta Data supports derived queries by method name conventions, pagination, and custom queries using `@Query` annotations. If you have experience with Spring Data or Micronaut, you will be familiar with these features.

```java
@Repository
public interface PostRepository extends CrudRepository<Post, UUID> {
    Optional<Post> findByTitle(String title);

    Page<Post> findByTitleLike(String titleLike, PageRequest pageRequest);

    @Query("where title like :title")
    @OrderBy("title")
    Post[] byTitleLike(@Param("title") String title);
}
```

Additionally, Jakarta Data provides a collection of lifecycle annotations ([`Find`](https://jakarta.ee/specifications/data/1.0/apidocs/jakarta.data/jakarta/data/repository/find), [`Insert`](https://jakarta.ee/specifications/data/1.0/apidocs/jakarta.data/jakarta/data/repository/insert), [`Update`](https://jakarta.ee/specifications/data/1.0/apidocs/jakarta.data/jakarta/data/repository/update), [`Delete`](https://jakarta.ee/specifications/data/1.0/apidocs/jakarta.data/jakarta/data/repository/delete)) that allow you to write operation methods more freely in your own interfaces. The entity type can be detected by the method parameters or return type.

```java
@Repository
public interface Blogger {
    @Query("""
            SELECT p.id, p.title FROM Post AS p
            WHERE p.title LIKE :title
            ORDER BY p.createdAt DESC
            """)
    Page<PostSummary> allPosts(@Param("title") String title, PageRequest page);

    @Find
    @OrderBy("createdAt")
    List<Post> byStatus(Status status, Order<Post> order, Limit limit);

    @Find
    Optional<Post> byId(UUID id);

    @Insert
    Post insert(Post post);

    @Update
    Post update(Post post);

    @Delete
    void delete(Post post);
}
```

Currently, Quarkus and Micronaut have already integrated Jakarta Data as an alternative persistence solution for developers. I have written articles introducing [the integration of Jakarta Data with Quarkus](https://itnext.io/integrating-jakarta-data-with-quarkus-0d18365a86fe) and [Micronaut](https://itnext.io/seamless-data-access-micronaut-data-embraces-jakarta-data-2f16f5a64c9e). Spring and Spring Data have no plans to integrate Jakarta Data, but that does not mean integrating Jakarta Data with Spring is difficult. I also wrote a post about [integrating Hibernate Data Repositories with Spring](https://itnext.io/integrating-jakarta-data-with-spring-0beb5c215f5f).

Unlike Jakarta Persistence, Spring Data, and Micronaut Data, Jakarta Data 1.0 does not provide specific annotations to define entity types. As a result, it relies heavily on the implementation details of each provider. For example, Micronaut Data reuses Jakarta Persistence annotations as well as its own data annotations, both of which work seamlessly with Jakarta Data. Quarkus and WildFly integrate Jakarta Data through Hibernate Data repositories, so in these environments, Jakarta Persistence entities are used to represent entities for Jakarta Data.

Currently, open-source Jakarta EE implementors such as GlassFish, WildFly, and Open Liberty are working on their own Jakarta Data implementations, typically leveraging entities defined with Jakarta Persistence. However, their approaches vary. WildFly (with Hibernate) translates Jakarta Data queries into Java code and generates repository implementations at compile time. In contrast, GlassFish reuses the effort from Eclipse JNoSQL and processes the queries dynamically at runtime.

In this post, we’ll focus on demonstrating Jakarta Data features on standard Jakarta EE-compatible application servers, such as GlassFish, WildFly, and others.

You can [get the example project](https://github.com/hantsy/jakartaee11-sandbox/tree/master/data) from my GitHub and explore it yourself.

## WildFly

WildFly has provided Jakarta Data as a preview feature since version 34. In the latest WildFly 37 preview, Jakarta Data support has been updated to align with Hibernate 7 and Jakarta Persistence 3.2.

To use Jakarta Data in WildFly, configure the `hibernate-processor` in your Maven compiler plugin. This processes your `Repository` interfaces and generates implementation classes at compile time.

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>${maven-compiler-plugin.version}</version>
    <configuration>
        <annotationProcessorPaths>
            <annotationProcessorPath>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>${lombok.version}</version>
            </annotationProcessorPath>
            <annotationProcessorPath>
                <groupId>org.hibernate.orm</groupId>
                <artifactId>hibernate-processor</artifactId>
                <version>${hibernate.version}</version>
            </annotationProcessorPath>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

Open a terminal window, navigate to the project root folder, run the following command to compile the project. This will generate repository source code using the configured *Hibernate Processor*:

```bash
mvn clean compile -Pwildfly
```

The generated repository implementation classes use Hibernate’s `StatelessSession` to implement all methods. For example, the generated `target/generated-sources/annotations/com/example/repository/PostRepository_.java` file looks like this:

```java
/**
 * Implements Jakarta Data repository {@link com.example.repository.PostRepository}
 **/
@Dependent
@Generated("org.hibernate.processor.HibernateProcessor")
public class PostRepository_ implements PostRepository {
    protected @Nonnull StatelessSession session;

    public PostRepository_(@Nonnull StatelessSession session) {
        this.session = session;
    }

    public @Nonnull StatelessSession session() {
        return session;
    }

    @PersistenceUnit
    private EntityManagerFactory sessionFactory;

    @PostConstruct
    private void openSession() {
        session = sessionFactory.unwrap(SessionFactory.class).openStatelessSession();
    }

    @PreDestroy
    private void closeSession() {
        session.close();
    }

    @Inject
    PostRepository_() {
    }

    //... other methods
    /**
     * Find {@link Post}.
     *
     * @see com.example.repository.PostRepository#findAll(PageRequest,Order)
     **/
    @Override
    public Page<Post> findAll(PageRequest pageRequest, Order<Post> sortBy) {
        var _builder = session.getCriteriaBuilder();
        var _query = _builder.createQuery(Post.class);
        var _entity = _query.from(Post.class);
        _query.where(
        );
        var _spec = SelectionSpecification.create(_query);
        for (var _sort : sortBy.sorts()) {
            _spec.sort(asc(Post.class, _sort.property())
                        .reversedIf(_sort.isDescending())
                        .ignoringCaseIf(_sort.ignoreCase()));
        }
        try {
            long _totalResults =
                    pageRequest.requestTotal()
                            ? _spec.createQuery(session)
                                    .getResultCount()
                            : -1;
            var _results = _spec.createQuery(session)
                .setFirstResult((int) (pageRequest.page()-1) * pageRequest.size())
                .setMaxResults(pageRequest.size())
                .getResultList();
            return new PageRecord<>(pageRequest, _results, _totalResults);
        }
        catch (PersistenceException _ex) {
            throw new DataException(_ex.getMessage(), _ex);
        }
    }
}
```

To run the project on a managed WildFly server, execute the following command:

```bash
mvn clean package wildfly:run -Pwildfly
```

You can find Jakarta Data usage examples in the [testing codes](https://github.com/hantsy/jakartaee11-sandbox/tree/master/data/src/test).

The tests are written with [Arquillian](https://www.arquillian.org) and JUnit 5, to run the tests on the managed WildFly with the Arquillian WildFly adapter:

```bash
mvn clean verify -Parq-wildfly-managed
```

## GlassFish

TBD

