# Migrating to Jakarta EE 11

Jakarta EE 11 continuously improves CDI alignments and developer productivity.

## History 

Since the Eclipse Foundation took over Java EE development, Jakarta EE has evolved steadily.

* Jakarta EE 8/9 did not introduce new features. Jakarta EE 8 mainly renamed the Maven coordinates from `javax` to `jakarta`, and Jakarta EE 9/9.1 cleaned up the new `jakarta` namespace at the API source code level.
* Jakarta EE 10 introduced new features such as a new Core Profile, a new Jakarta Contexts and Dependency Injection Lite specification, and enhancements to existing specifications. Jakarta EE 10 requires Java 11 as the minimum and supports Java 17 at runtime.
* Jakarta EE 11 continuously improves the developer experience and deprecates more APIs. Jakarta EE 11 updates Java 17 as the baseline and supports Java 21 at runtime.

Let's have a look at what's new in Jakarta EE 11.

## What's New In Jakarta EE 11

### New Specifications

Jakarta EE 11 added a new specification: *Jakarta Data*.

*Jakarta Data* was incubated two years ago and has always been under active development. Eventually, it joined the Jakarta EE 11 release train. Go to the [Jakarta Data specification](https://jakarta.ee/specifications/data/1.0/jakarta-data-1.0) to learn it from scratch. The Hibernate reference document also includes a chapter: [Hibernate Data Repositories](https://docs.jboss.org/hibernate/orm/7.0/repositories/html_single/Hibernate_Data_Repositories.html) to demonstrate Jakarta Data usage by examples.

### Updates

The `record` type is now a first-class citizen in Jakarta EE. Specifications like Persistence, Faces, Expression Language, and Validation have embraced `record` types. Unfortunately, JSON-B has not been updated to support `record` types (although the implementation in GlassFish does support it). Additionally, using `record` types in Messaging payloads can be problematic if they do not implement `Serialization`, see [jakartaee/messaging#343](https://github.com/jakartaee/messaging/issues/343).

Several specifications were updated in Jakarta EE 11.

*CDI* - the core of the Jakarta EE ecosystem, includes a few minor enhancements in the new 4.1 version. For developers, the ability to use `@Priority` on producers is a significant highlight. For implementations, the major change is that the CDI EE integration part has been moved to the Jakarta platform specification. Check out [What's new in CDI 4.1](https://jakartaee.github.io/cdi/2024/02/27/whats-new-in-cdi41.html) for more details.

*Persistence* 3.2 added many small improvements, including adding the `record` type as embeddable classes, porting more SQL-specific functions to JPQL, and providing programmatic configuration instead of the `persistence.xml`.

*Concurrency* 3.1 adopted Java 21 virtual threads in managed execution service and Java 9 `Flow` (aka ReactiveStreams support) in context propagation, and also added a new `@Schedule` (nested in `@Asynchronous`) to replace the existing one in the legacy EJB specification.

*REST* 4.0 added JSON Merge Patch support.

*Security* 4.0 introduced a new in-memory `IdentityStore`, which is very useful in development. It also added the ability to handle multiple authentication mechanisms.

The `ManagedBean` specification has been removed from Jakarta EE 11. Additionally, starting with Jakarta EE 11, the SOAP-based *Web Service* specification is marked as *deprecated* and is not recommended for new projects. References to `SecurityManager` are planned for removal due to its deprecation in a future Java SE runtime. Currently, only some specifications have accomplished the `SecurityManager` API cleanup, with the remainder delayed to Jakarta EE 12.

We will explore the new features with example code in future posts.

### Under Maintenance

The following specifications have no updates in Jakarta EE 11:

* Jakarta JSON-B
* Jakarta JSON-P
* Jakarta Messaging
* Jakarta Batch
* Jakarta Activation
* Jakarta Mail

## The Future of Jakarta EE

Now that Jakarta EE is community-driven and community-led, everyone can participate in the progress of the specification definitions.

A few features have been proposed in discussions and may become specifications in the future, for example:

* Jakarta RPC - standardizes gRPC in the Jakarta EE ecosystem.
* Jakarta NoSQL - introduces a general-purpose `Repository` pattern and `Template` programmatic APIs for NoSQL databases.
* Jakarta MVC - based on the existing Jakarta REST specification, provides an action-based web framework similar to the popular Apache Struts/Spring MVC. NOTE: It is not a replacement for Faces.
* Common HTTP APIs - [The topic has been discussed in Jakarta Platform issues](https://github.com/jakartaee/platform/issues/673), but there is no specification proposal submitted yet. Currently, the Faces specification reuses the Servlet API, and the MVC specification reuses the REST API. It would be better to introduce a collection of common HTTP APIs to unify HTTP handling.

For more information about Jakarta EE, please visit the [official Jakarta EE homepage](https://jakarta.ee).
