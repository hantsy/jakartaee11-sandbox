# Migrating to Jakarta EE 11

Jakarta EE 11 continuously improves CDI alignments and the developer's productivity.

## History 

Since Eclipse Foundation handed over the Java EE development, Jakarta EE has evolved steadily. 

 * Jakarta EE 8 mainly resolved the trademark issues, and renamed the Maven coordinates from `javax` to `jakarta`. 
 * Jakarta EE 9/9.1 cleaned up the new Jakarta namespace at the API source code level.
 * Jakarta EE 10 updated a collection of specifications to align with the new Java runtime requirement. Jakarta EE 10 API requires Java 11 as the minimal and also supports Java 17 at runtime.
 * Jakarta EE 11 continuously improved the developer's experience and deprecated more APIs. Jakarta EE 11 requires Java 17 as the minimal and also supports Java 21 at runtime.

Let's have a look at what's new in Jakarta EE 11.

## What's New In Jakarta EE 11

Jakarta Data was incubated two years ago and is always under active development. And finally, it joined the Jakarta EE 11 family. 

CDI is the kernel of Jakarta EE ecosystem, Faces, REST, Concurrency, Persistence, etc. add more alignments to the new CDI.

The Persistence specification adds the `record` type as embeddable classes, ports more SQL-specific functions to JPQL, and provides programmatic configuration instead of the `persistence.xml`. 

The Concurrency specification adds optional Java 21 virtual thread support and also adds a new `@Scheduled` to replace the existing one in the legacy EJB specification.

The REST specification adds JSON Patch support.

Security 3.1 supports in-memory `identity store` and adds multiple HTTP authentication mechanisms.

The Faces, EL, Validation, etc. also add `record` type support. 

Jakarta EE 11 removes the `ManagedBean` specification, deprecates SOAP-based `Web Service`, and cleans up the reference for `SecurityManager` which will be removed in a further Java version.

We will explore the new features by example codes in future posts.

## The Future of Jakarta EE

Now Jakarta EE is a community-led specification, everybody can participate in the progress of the specification definitions.

A few proposals have been submitted, for example.

* Jakarta RPC  - standardizes the gRPC in the Jakarta EE ecosystem.
* Jakarta NoSQL  - introduces a general-purpose `Repository` pattern and `Template` programmatic APIs for the NoSQL database.
* Jakarta MVC - based on the existing Jakarta REST, provides an action-based web framework similar to the popular Apache Struts/Spring MVC. NOTE: It is not a replacement for Faces.
* Common HTTP APIs - The idea has been mentioned in the community discussion, there is no specification proposal now. Currently Faces specification reuses Servlet API, and MVC specification reuses REST API. It is better to introduce a collection of common HTTP APIs to unite HTTP handling. 

For more information about Jakarta EE, please navigate to the [official Jakarta EE homepage](https://jakarta.ee).
