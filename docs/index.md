# Migrating to Jakarta EE 11

Jakarta EE 11 continuously improves CDI alignments and the developer's productivity.

Let's have a look at what's new in Jakarta EE 11.

## What's New In Jakarta EE 11

 Since Eclipse Foundation handed over the Java EE development, there are a lot of improvements added in the latest Jakarta EE. 
 * Jakarta EE 8 mainly resolved the trademark issues, and renamed the Maven coordinates from `javax` to `jakarta`. 
 * Jakarta EE 9/9.1 cleaned up the new Jakarta namespace in API source code level.
 * And Jakarta EE 10 updated a collection of specifications to align with the new Java runtime requirement. Jakarta EE 10 APIs requires Java 11 as the minimal and also supports Java 17 at runtime.
 * Jakarta EE 11 continuously improve the developer's experience and deprecated more APIs. Jakarta EE 11 requires Java 17 as the minimal and also support Java 21 at runtime.

Jakarta Data was incubated in the past two years, and finally joins Jakarta EE 11 family. 

CDI is the kernel of Jakarta EE ecosystem, Faces, REST, Concurrency, Persistence etc. add more alignments to the new CDI.

The Persistence specification adds `record` type as embeddable classes, and ports more SQL specific functions to JPQL, and provides programmatic configuration instead of the `persistence.xml`. 

The Concurrency specification add optional Java 21 virtual thread support, also adds a new `@Scheduled` to replace the existing annotation in the legacy EJB specification.

The REST specification adds JSON Patch support.

Security 3.1 supports in-memory identity store, and adds multiple http handlers.

The Faces, EL, Validation, etc. also add `record` type support. 

Jakarta EE 11 removes `ManagedBean` specification, deprecates SOAP-based `Web Service`, and cleans up the reference of Java `SecurityManager` which will be removed in a further Java version.

We will explore the new features by example codes in the future posts.

## The Future of Jakarta EE

Now Jakarta EE is a community-leaded specification, everybody can join and participate into the progress of the definition of the specifications.

There are a few proposals are submitted, for example.

* Jakarta RPC  - standardizes the gRPC in Jakarta EE ecosystem.
* Jakarta NoSQL  - introduces a general-purpose `Repository` pattern and `Template` programmatic APIs for NoSQL database.
* Jakarta MVC - bases on the existing Jakarta REST, provides Apache Struts/Spring MVC like action base web framework. NOTE, it is not a replacement of Faces.
* Common HTTP APIs - This idea is mentioned in community discussion, there is no specification proposal now.  Currently Faces specification reuses Servlet API, and MVC specification reuses REST API, it is better to introduce a collection of common HTTP APIs to unite HTTP handling. 

More info about Jakarta EE, please go to the [official Jakarta EE homepage](https://jakarta.ee).
