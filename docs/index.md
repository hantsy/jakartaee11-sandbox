# Migrating to Jakarta EE 11

Jakarta EE 11 continuously improves CDI alignments and the developer's productivity.

## History 

Since Eclipse Foundation handed over the Java EE development, Jakarta EE has evolved steadily. 

 * Jakarta EE 8 mainly resolved the trademark issues, and renamed the Maven coordinates from `javax` to `jakarta`. 
 * Jakarta EE 9/9.1 cleaned up the new Jakarta namespace at the API source code level.
 * Jakarta EE 10 updated a collection of specifications to align with the new Java runtime requirement. Jakarta EE 10 requires Java 11 as the minimal and supports Java 17 at runtime.
 * Jakarta EE 11 continuously improved the developer's experience and deprecated more APIs. Jakarta EE 11 updates Java 17 as the minimal and also supports Java 21 at runtime.

Let's have a look at what's new in Jakarta EE 11.

## What's New In Jakarta EE 11

*Jakarta Data* was incubated two years ago and is always under active development. Eventually, it joined the Jakarta EE 11 family. 

*CDI* is the kernel of the Jakarta EE ecosystem. The new CDI 4.1 did not bring huge changes, just added some small enhancements. For developers, allowing `@Priorty` on producers is a highlight. For implementations, the EE integration was moved to the platform specification. Check [What's new in CDI 4.1?](https://jakartaee.github.io/cdi/2024/02/27/whats-new-in-cdi41.html) for more details.

*Persistence* 3.2 added many small improvements, including adding the `record` type as embeddable classes, porting more SQL-specific functions to JPQL, and providing programmatic configuration instead of the `persistence.xml`.   

*Concurrency* specification added optional Java 21 virtual thread support and a new `@Scheduled` to replace the existing one in the legacy EJB specification.

The *REST* specification added JSON Patch support.

*Security* 3.1 introduced a new in-memory `identity store` which is very useful in development. It also added multiple HTTP authentication mechanisms.

Faces, EL, Validation, etc. also add `record` type support. 

Jakarta EE 11 removed the `ManagedBean` specification, deprecated the SOAP-based *Web Service* specification, and cleaned up the reference for `SecurityManager` because it will be removed in a future Java SE runtime.

We will explore the new features by example codes in future posts.

## The Future of Jakarta EE

Now Jakarta EE is a community-led specification, everybody can participate in the progress of the specification definitions.

A few proposals have been submitted, for example.

* Jakarta RPC  - standardizes the gRPC in the Jakarta EE ecosystem.
* Jakarta NoSQL  - introduces a general-purpose `Repository` pattern and `Template` programmatic APIs for the NoSQL database.
* Jakarta MVC - based on the existing Jakarta REST, provides an action-based web framework similar to the popular Apache Struts/Spring MVC. NOTE: It is not a replacement for Faces.
* Common HTTP APIs - The idea has been mentioned in the community discussion, there is no specification proposal now. Currently Faces specification reuses Servlet API, and MVC specification reuses REST API. It is better to introduce a collection of common HTTP APIs to unite HTTP handling. 

For more information about Jakarta EE, please navigate to the [official Jakarta EE homepage](https://jakarta.ee).
