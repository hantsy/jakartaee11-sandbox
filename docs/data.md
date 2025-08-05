# Meet Jakarta Data: The Newest Member of the Jakarta EE 11 Ecosystem

[Jakarta Data](https://jakarta.ee/specifications/data/1.0/) is a new specification added in Jakarta EE 11. Jakarta Data provides a collection of new APIs to ease data access, but is not limited to the existing Jakarta Persistence and relational databases. It is designated neutrally for underlying data storage. The [Eclipse JNoSQL project](https://www.jnosql.org/) also implements this specification and brings the same experience to the NoSQL world.

Similar to the [Spring Data Commons](https://github.com/spring-projects/spring-data-commons), [Micronaut Data](https://github.com/micronaut-projects/micronaut-data), and [Quarkus Panache](https://quarkus.io/guides/hibernate-orm-panache), firstly Jakarta Data introduces a `Repository` abstraction, including a basic [`DataRepository`](https://jakarta.ee/specifications/data/1.0/apidocs/jakarta.data/jakarta/data/repository/datarepository) interface to inidicate which is a Repository,  and two interfaces [`BasicRepository`](https://jakarta.ee/specifications/data/1.0/apidocs/jakarta.data/jakarta/data/repository/basicrepository) and [`CrudRepository`](https://jakarta.ee/specifications/data/1.0/apidocs/jakarta.data/jakarta/data/repository/crudrepository) to envelop the common `CRUD` operations for underlying data storage. It also introduces a new annotation `@Repository` to mark an interface as the `Repository` role, whether it is derived from the common interfaces or a pure interface that does not extend any existing interface.

For example, the following `PostRepository` interface is for the entity `Post`.

```java
@Repository
public interface PostRepository extends CrudRepository<Post, UUID> {

}
```

Besides these, it also supports derived queries by method name conventions, paginations, and custom queries by `@Query` annotations, all of which you could be familiar with if you have some experience with Spring Data or Micronaut. 

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

Additionally, Jakarta Data provides a collection of lifecycle annotations( [`Find`](https://jakarta.ee/specifications/data/1.0/apidocs/jakarta.data/jakarta/data/repository/find), [`Insert`](https://jakarta.ee/specifications/data/1.0/apidocs/jakarta.data/jakarta/data/repository/insert), [`Update`](https://jakarta.ee/specifications/data/1.0/apidocs/jakarta.data/jakarta/data/repository/update), [`Delete`](https://jakarta.ee/specifications/data/1.0/apidocs/jakarta.data/jakarta/data/repository/delete)) to allow you to write operation methods more freely in your own interface, which will detect the entity type by the method parameters or return result type. 

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

Currently, Quakrus and Micronaut have already integrated Jakarta Data tightly as an alternative persistence solution for developers. I have written some articles to introduce [the integration of Jakarta Data with Quarkus](https://itnext.io/integrating-jakarta-data-with-quarkus-0d18365a86fe) and [Micronaut](https://itnext.io/seamless-data-access-micronaut-data-embraces-jakarta-data-2f16f5a64c9e). Spring and Spring Data have no plan to integrate Jakarta Data, but it does not mean integrating Jakarta Data with Spring is hard. I also wrote a post to introduce [integrating Hibernate Data Repositories with Spring](https://itnext.io/integrating-jakarta-data-with-spring-0beb5c215f5f). 

In this post, we will focus on demonstrating Jakarta Data features on standard Jakarta EE compatible application servers, such as GlassFish, WildFly, etc. 
