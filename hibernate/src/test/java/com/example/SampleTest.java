package com.example;

import com.example.addressbook.Person;
import com.example.addressbook.Person_;
import com.example.blog.Comment;
import com.example.blog.Post;
import com.example.blog.Post_;
import com.example.bookstore.Author;
import com.example.bookstore.Book;
import com.example.bookstore.Isbn;
import com.example.record.RecordEmbeddedEntity;
import com.example.record.RecordEmbeddedIdEntity;
import com.example.record.RecordIdClassEntity;
import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaSelect;
import jakarta.persistence.criteria.Root;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;


public class SampleTest {
    private static final Logger LOG = LoggerFactory.getLogger(SampleTest.class);

    @Test
    public void testWithPersistenceXML() {
        try (var emf = Persistence.createEntityManagerFactory("bookstorePU")) {

            emf.runInTransaction(em -> {
                Book entity = new Book(
                        new Isbn("9781932394887"),
                        "Java Persistence with Hibernate",
                        new Author("Gavin King"),
                        new BigDecimal("50.1234")
                );
                em.persist(entity);
                LOG.debug("persisted book: {}", entity);

                var book =
                        em.find(Book.class, new Isbn("9781932394887"),
                                Map.of("jakarta.persistence.cache.retrieveMode",
                                        CacheRetrieveMode.BYPASS,
                                        "jakarta.persistence.query.timeout", 500,
                                        "org.hibernate.readOnly", true)
                        );
                LOG.debug("found book with Map properties: {}", book);

                // type safe options
                var result = em.find(Book.class, new Isbn("9781932394887"),
                        CacheRetrieveMode.BYPASS,
                        Timeout.seconds(500),
                        LockModeType.READ);
                LOG.debug("found book result with type-safe options: {}", result);

                // get persistent or detached instance
                var ref = em.getReference(result);
                LOG.debug("book ref: {}", ref);


                var nullableReult = em.createQuery("from Book where id=:isbn", Book.class)
                        .setParameter("isbn", new Isbn("9781932394887"))
                        .getSingleResultOrNull();
                LOG.debug("book getSingleResultOrNull result: {}", nullableReult);
            });

            emf.callInTransaction(em -> em.createQuery("from Book", Book.class)
                            .getResultList())
                    .forEach(book -> LOG.debug("saved book: {}", book));

        }
    }


    @Test
    public void testJPQLImprovements() {
        try (var emf = Persistence.createEntityManagerFactory("bookstorePU")) {

            emf.runInTransaction(em -> {
                //persist new book
                Book entity = new Book(
                        new Isbn("9781932394887"),
                        "Java Persistence with Hibernate",
                        new Author("Gavin King"),
                        new BigDecimal("50.1234")
                );
                em.persist(entity);
                LOG.debug("persisted book: {}", entity);

                // query without select
                em.createQuery("from Book where name like '%Hibernate'", Book.class)
                        .getResultStream()
                        .forEach(book -> LOG.debug("query result without select:{}", book));

                // new functions
                // count(this): org.hibernate.query.SemanticException: Could not interpret path expression 'this'
                var count = em.createQuery("select count(this) from Book")
                        .getSingleResult();
                LOG.debug(" count(this) result:{}", count);

                // id and version function
                em.createQuery("select id(this), version(this) from Book", Object[].class)
                        .getResultList()
                        .forEach(book -> LOG.debug("id and version result:{}", book));

                em.createQuery("""
                                select left(name, 5),
                                right(name, 2),
                                cast(price as Integer),
                                replace(name ,' ','_'),
                                name
                                from Book
                                """, Object[].class)
                        .getResultStream()
                        .forEach(book -> LOG.debug("new functions result:{}", Stream.of(book).toList()));

                // improved sort nulls first/last
                em.createQuery("from Book order by name nulls first", Book.class)
                        .getResultStream()
                        .forEach(book -> LOG.debug("improved sort nulls first:{}", book));

                // persist new customers
                var person = new Person("Gavin", "King");
                em.persist(person);
                LOG.debug("persisted person: {}", person);
                var person2 = new Person("Hantsy", "Bai");
                em.persist(person2);
                LOG.debug("persisted person2: {}", person2);

                // query book author name equals person firstName and lastName
                em.createQuery("""
                                select b  from Book b cross join Person c
                                where b.author.name = c.firstName ||' '|| c.lastName
                                and c.firstName=:firstName
                                and c.lastName=:lastName
                                """, Book.class)
                        .setParameter("firstName", "Gavin")
                        .setParameter("lastName", "King")
                        .getResultStream()
                        .forEach(book -> LOG.debug("query book author name equals person firstName and lastName: {}", book));

                // query union book name and person name
                em.createQuery("""
                                select c.firstName ||' '|| c.lastName from Person c
                                union
                                select b.author.name  from Book b
                                """, String.class)
                        .getResultStream()
                        .forEach(name -> LOG.debug("query union book name and person name: {}", name));

                // intersect book name and person name
                em.createQuery("""
                                select c.firstName ||' '|| c.lastName from Person c
                                intersect
                                select b.author.name  from Book b
                                """, String.class)
                        .getResultStream()
                        .forEach(name -> LOG.debug("intersect book name and person name: {}", name));

                // except book name and person name
                em.createQuery("""
                                select c.firstName ||' '|| c.lastName from Person c
                                except
                                select b.author.name  from Book b
                                """, String.class)
                        .getResultStream()
                        .forEach(name -> LOG.debug("except book name and person name: {}", name));


                /////////////////////////////////////////
                // an example of using CriteriaSelect,
                /////////////////////////////////////////

                CriteriaBuilder cb = em.getCriteriaBuilder();

                // First part of the union: select c.firstName || ' ' || c.lastName from Person c
                CriteriaQuery<String> personQuery = cb.createQuery(String.class);
                Root<Person> personRoot = personQuery.from(Person.class);
                personQuery.select(cb.concat(List.of(personRoot.get(Person_.FIRST_NAME), cb.literal(" "), personRoot.get(Person_.LAST_NAME))));

                // Second part of the union: select b.author.name from Book b
                CriteriaQuery<String> bookQuery = cb.createQuery(String.class);
                Root<Book> bookRoot = bookQuery.from(Book.class);
                bookQuery.select(bookRoot.get("author").get("name"));

                // Combine the two queries with UNION
                // Jakarta Persistence 3.2 adds union() to CriteriaBuilder
                CriteriaSelect<String> unionQuery = cb.union(personQuery, bookQuery);

                em.createQuery(unionQuery)
                        .getResultStream()
                        .forEach(name -> LOG.info("query union book name and person name: " + name));

            });

        }
    }

    @Test
    public void testSchemaExport() {
        try (var emf = Persistence.createEntityManagerFactory("bookstorePU")) {
            // schema export
            LOG.debug("exporting schema...");
            emf.getSchemaManager().truncate();
            emf.getSchemaManager().drop(true);
            emf.getSchemaManager().create(true);
        }
    }

    @Test
    public void testMappingAnnotations() {
        try (var emf = Persistence.createEntityManagerFactory("bookstorePU")) {
            emf.runInTransaction(em -> {
                // persist new Post entity
                Post entity = new Post("What's new in Persistence 3.2?",
                        "dummy content of Jakarta Persistence 3.2");
                entity.addComment(new Comment("dummy comment by addComment method"));
                em.persist(entity);
                LOG.debug("persisted Post: {}", entity);

                // persist comment
                var comment = new Comment(entity, "dummy comment");
                em.persist(comment);
                LOG.debug("persisted comment: {}", comment);
            });
            emf.runInTransaction(em -> {
                var entity = em.createQuery("from Post", Post.class).getResultList().getFirst();
                LOG.debug("query result: {}", entity);
                // query byTitle named query
                var result = em.createNamedQuery(Post_.QUERY_BY_TITLE, Post.class)
                        .setParameter("title", "What's new in Persistence 3.2?")
                        .getSingleResult();
                LOG.debug("query byTitle result: {}", result);

                // query withComments entityGraph
                Post result2 = (Post) em.find(em.getEntityGraph(Post_.GRAPH_WITH_COMMENTS), entity.getId());
                LOG.debug("query withComments result: {}", result2.getComments());

                // query withComments entityGraph programmatically
                var postEntityGraph = em.createEntityGraph("withComments");
                postEntityGraph.addAttributeNode("comments");
                Post result3 = (Post) em.find(postEntityGraph, entity.getId());
                LOG.debug("query withComments programmatically result: {}", result3.getComments());
            });
        }
    }

    @Test
    public void testRecord() {
        try (var emf = Persistence.createEntityManagerFactory("bookstorePU")) {
            emf.runInTransaction(em -> {
                // persist MyClassIdEntity
                RecordIdClassEntity entity = new RecordIdClassEntity(new RecordIdClassEntity.RecordIdClass("test1", "test2"));
                em.persist(entity);
                LOG.debug("persisted MyClassIdEntity: {}", entity);

                // persist MyEmbeddedIdEntity
                RecordEmbeddedIdEntity entity2 = new RecordEmbeddedIdEntity(new RecordEmbeddedIdEntity.RecordId("test1"));
                em.persist(entity2);
                LOG.debug("persisted MyEmbeddedIdEntity: {}", entity2);

                // persist MyEmbeddedEntity
                RecordEmbeddedEntity entity3 = new RecordEmbeddedEntity(new RecordEmbeddedEntity.RecordEmbedded("test1", 40));
                em.persist(entity3);
                LOG.debug("persisted MyEmbeddedEntity: {}", entity3);
            });
        }
    }

    @Test
    public void testEMCallbacks() {
        try (var emf = Persistence.createEntityManagerFactory("bookstorePU")) {
            emf.runInTransaction(em -> {
                // persist new Post entity
                Post entity = new Post("What's new in Persistence 3.2?",
                        "dummy content of Jakarta Persistence 3.2");
                em.persist(entity);
                LOG.debug("persisted Post: {}", entity);
            });

            var em = emf.createEntityManager();
            em.runWithConnection((Connection conn) -> {
                var rs = conn.prepareStatement("select * from posts").executeQuery();
                while (rs.next()) {
                    LOG.debug("query result:");
                    LOG.debug("id: {}", rs.getLong("id"));
                    LOG.debug("title: {}", rs.getString("title"));
                    LOG.debug("content: {}", rs.getString("content"));
                }
            });

            EntityTransaction tx = em.getTransaction();
            tx.begin();
            try {
                Post entity = new Post("What's new in Jakarta EE 11?",
                        "dummy content of What's new in Jakarta EE 11");
                em.persist(entity);
            } catch (Exception e) {
                tx.rollback();
            } finally {
                tx.commit();
            }
        }
    }

    @Test
    public void testPersistenceConfiguration() {
        PersistenceConfiguration configuration = new PersistenceConfiguration("bookstore")
                .transactionType(PersistenceUnitTransactionType.RESOURCE_LOCAL)
                .provider(HibernatePersistenceProvider.class.getName())
                // .nonJtaDataSource("java:global/jdbc/BookstoreData")
                .managedClass(Book.class)
                .managedClass(Isbn.class)
                .managedClass(Author.class)
                .property(PersistenceConfiguration.LOCK_TIMEOUT, 5000)
                .property("hibernate.type.prefer_java_type_jdbc_types", true)
                .property("hibernate.hbm2ddl.auto", "create-drop")
                .property(PersistenceConfiguration.JDBC_URL, "jdbc:h2:mem:db1;DB_CLOSE_DELAY=-1")
                .property(PersistenceConfiguration.JDBC_DRIVER, "org.h2.Driver")
                .property(PersistenceConfiguration.JDBC_USER, "sa");


        // val emf =configuration.createEntityManagerFactory();
        try (var emf = Persistence.createEntityManagerFactory(configuration)) {

            emf.runInTransaction(em -> {
                Book entity = new Book(
                        new Isbn("9781932394887"),
                        "Java Persistence with Hibernate",
                        new Author("Gavin King"),
                        new BigDecimal("50.1234")
                );
                em.persist(entity);
            });

            emf.callInTransaction(em -> em.createQuery("from Book", Book.class).getResultList())
                    .forEach(book -> LOG.debug("saved book: {}", book));

//
//            Persistence.generateSchema(
//                    "bookstore",
//                    Map.of(
//                            "jakarta.persistence.schema-generation.scripts.action", "drop-and-create",
//                            "jakarta.persistence.schema-generation.scripts.create-target", "/tmp/schema.sql",
//                            "jakarta.persistence.schema-generation.scripts.drop-target", "/tmp/drop.sql"
//                    )
//            );

        }
    }
}


