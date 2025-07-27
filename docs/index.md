# Welcome to Jakarta EE 11

Followed the releases of the Jakarta EE 11 [Core Profile](https://www.infoq.com/news/2025/01/jakarta-ee-11-core-profile/) and [Web Profile](https://foojay.io/today/jakarta-ee-11-web-profile-released-enabled-by-eclipse-glassfish/) in the past months, the Eclipse Foundation has now officially launched the [final version of the Jakarta EE 11 platform](https://newsroom.eclipse.org/news/announcements/eclipse-foundation%E2%80%99s-jakarta-ee-working-group-announces-jakarta-ee-11-release).

## History

Since the Eclipse Foundation took over Java EE, Jakarta EE has continued to evolve at a steady pace.

* **Jakarta EE 8/9**: These versions focused on foundational changes, with Jakarta EE 8 primarily renaming Maven coordinates from `javax` to `jakarta`, and Jakarta EE 9/9.1 cleaning up the new `jakarta` namespace at the API source code level.
* **Jakarta EE 10**: This release introduced significant new features, including the Core Profile, the Jakarta Contexts and Dependency Injection Lite specification, and enhancements to existing specifications. Jakarta EE 10 set Java 11 as the minimum requirement and added support for Java 17 at runtime.
* **Jakarta EE 11**: The latest release continues to improve the developer experience and further deprecates outdated APIs. Jakarta EE 11 now uses Java 17 as its baseline and supports Java 21 at runtime. For specification providers, this release modernizes the Jakarta EE TCK by updating it to the latest Arquillian and JUnit 5, making it easier for more providers to align with the new specifications.

Let’s take a closer look at what’s new in Jakarta EE 11.

## What's New In Jakarta EE 11

### New Specifications

Jakarta EE 11 introduces a major new specification: *Jakarta Data*.

*Jakarta Data* has been in incubation for two years and has seen active development throughout. Now, it officially joins the Jakarta EE 11 release train. To get started, check out the [Jakarta Data specification](https://jakarta.ee/specifications/data/1.0/jakarta-data-1.0). The Hibernate documentation also features a dedicated chapter, [Hibernate Data Repositories](https://docs.jboss.org/hibernate/orm/7.0/repositories/html_single/Hibernate_Data_Repositories.html), with practical examples of Jakarta Data in action.

### Updates

The Java `record` type is now a first-class citizen in Jakarta EE. Specifications such as Persistence, Faces, Expression Language, and Validation have all embraced `record` types. However, JSON-B has not yet been updated to support records (though GlassFish’s implementation does), and using records as Messaging payloads can be problematic if they do not implement `Serialization`—see [jakartaee/messaging#343](https://github.com/jakartaee/messaging/issues/343) for details.

Several specifications have received notable updates in Jakarta EE 11:

- **CDI 4.1**: The core of the Jakarta EE ecosystem now includes several enhancements. For developers, the ability to use `@Priority` on producers is a key highlight. For implementers, the CDI EE integration has been moved to the Jakarta platform specification. See [What's new in CDI 4.1](https://jakartaee.github.io/cdi/2024/02/27/whats-new-in-cdi41.html) for more information.
- **Persistence 3.2**: This release brings many small improvements, such as supporting `record` types as embeddable classes, adding more SQL-specific functions to JPQL, and enabling programmatic configuration as an alternative to `persistence.xml`.
- **Concurrency 3.1**: Now supports Java 21 virtual threads in managed execution services and Java 9 `Flow` (ReactiveStreams) in context propagation. It also introduces a new `@Schedule` annotation (nested in `@Asynchronous`) to replace the legacy EJB version.
- **REST 4.0**: Adds support for JSON Merge Patch, enhancing RESTful API capabilities.
- **Security 4.0**: Introduces a new in-memory `IdentityStore`—very useful for development—and adds support for multiple authentication mechanisms.

The `ManagedBean` specification has been removed from Jakarta EE 11. Additionally, starting with this release, the SOAP-based *Web Service* specification is marked as *deprecated* and is not recommended for new projects. References to `SecurityManager` are being phased out due to its deprecation in future Java SE runtimes. While some specifications have already removed `SecurityManager` APIs, the remainder will be addressed in Jakarta EE 12.

We will explore these new features with example code in future posts.

### Under Maintenance

The following specifications remain unchanged in Jakarta EE 11:

* Jakarta JSON-B
* Jakarta JSON-P
* Jakarta Messaging
* Jakarta Batch
* Jakarta Activation
* Jakarta Mail

## The Future of Jakarta EE

With Jakarta EE now fully community-driven, everyone can participate in shaping the future of the platform.

Several new features are under discussion and may become specifications in the future, including:

* **Jakarta RPC** – Standardizing gRPC support in the Jakarta EE ecosystem.
* **Jakarta NoSQL** – Introducing a general-purpose `Repository` pattern and `Template` APIs for NoSQL databases.
* **Jakarta MVC** – Providing an action-based web framework, similar to Apache Struts or Spring MVC, based on the existing Jakarta REST specification. (Note: This is not a replacement for Jakarta Faces.)
* **Common HTTP APIs** – [Discussions are ongoing](https://github.com/jakartaee/platform/issues/673) about introducing a unified set of HTTP APIs to streamline HTTP handling across specifications. Currently, Faces reuses the Servlet API, and MVC reuses the REST API.

For more information about Jakarta EE, please visit the [official Jakarta EE homepage](https://jakarta.ee).
