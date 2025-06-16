## Jakarta Persistence 

Jakarta Persistence (aka JPA) is a standard of persistence and O/R mapping in Java environments. It includes a collection of simple and stupid APIs to execute literal query (aka JPQL), and also contains alternative Criteria APIs to build the query clause in type-safe Java codes.

Jakarta Persistence 3.2 includes a large number of enhancements and additions, more info please refer the feature list of [Jakarta Persistence 3.2 specification page](https://jakarta.ee/specifications/persistence/3.2/).


# Programmatic API 

Before 3.2 in a Java SE environment, if you want to build a `EntityManagerFactory` object, you should provide a the *persistence.xml* in the `src/main/resources/META-INF` folder  via the new introduced  programmatic API instead of discovering  in the application archive. 



