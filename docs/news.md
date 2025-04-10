# Jakarta EE 11 Web Profile is GA: A New Era for Enterprise Java Development

In a highly anticipated move by the global Java development community, Jakarta EE 11 has officially been launched, marking a significant milestone in enterprise-level Java development. This release introduces a new phase for the framework, offering enhanced functionalities and optimized developer experiences.

> [!NOTE]
> As of the release announcement, the Jakarta Platform repository has been tagged with the official version, signifying the completion of all Jakarta EE 11 specifications, following the earlier releases of the Core Profile and Web Profile.

## The Evolution of Jakarta EE

Since the Eclipse Foundation took over the development of Java EE, Jakarta EE has undergone consistent evolution while maintaining its stability:

- **Jakarta EE 8 and 9**: Focused on migrating the namespace from `javax` to `jakarta` to resolve legal issues related to Java's trademark.
- **Jakarta EE 10**: Introduced the Core Profile, solidifying the importance of CDI and revitalizing the ecosystem. Spring 6 was aligned with this release.
- **Jakarta EE 11**: Advances developer experience, sets Java 17 as the baseline, and supports Java 21 runtime. The upcoming Spring 7 will align with this version.

## Key Highlights

### New Specification: Jakarta Data

A standout feature of Jakarta EE 11 is the introduction of **Jakarta Data**, dedicated to data processing. After two years of incubation and development, Jakarta Data officially joins the Jakarta EE ecosystem, filling a crucial gap in enterprise Java frameworks. For more details, visit the [Jakarta Data Specification Documentation](https://jakarta.ee/specifications/data/).

### Comprehensive Support for Java `record` Type

Jakarta EE 11 elevates Java's `record` type to a first-class citizen, significantly simplifying data modeling. Various specifications, including Persistence, Faces, Expression Language, and Validation, now fully support this feature.

### Major Specification Updates

- **CDI 4.1**: Introduces the ability to use `@Priority` with `@Produces`.
- **Concurrency 3.1**: Adds support for Java 21's virtual threads and Reactive Streams.
- **REST 4.0**: Includes JSON Merge Patch functionality.
- **Security 4.0**: Features an in-memory `IdentityStore` implementation and multi-authentication mechanisms.

### Specification Cleanup and Optimization

- The `ManagedBean` specification has been removed. All existing `ManagedBean` code must migrate to CDI.
- SOAP-based *Web Service* specifications are now deprecated and are no longer recommended for new projects.

## A Community-Driven Future

As a fully community-driven project, Jakarta EE's future is brimming with possibilities. Ongoing discussions around features like Jakarta RPC, Jakarta NoSQL, and Jakarta MVC may shape the next chapters of Jakarta EE's development.

## Join the Jakarta EE Journey

The release of Jakarta EE 11 is not just a step forward for enterprise Java but a leap for the developer community. For more information, explore:

- Official Website: [https://jakarta.ee](https://jakarta.ee)
- JakartaOne: [https://jakartaone.org](https://jakartaone.org). Stay tuned for [JakartaONE 2025 LiveStream](https://jakartaone.org/2025/) and the [Chinese Community Special Session](https://jakartaone.org/2025/chinese/).
- Slack: JakartaEE Development

If you are excited about the future of Jakarta EE, consider participating in the official [Developer Survey](https://www.surveymonkey.com/r/TanjaJakartaEE) to help shape its direction!
