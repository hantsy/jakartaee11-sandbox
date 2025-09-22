# Jakarta Expression Language

[Jakarta Expression Language 6.0](https://jakarta.ee/specifications/expression-language/6.0/) removes the dependency on `SecurityManager` and several deprecated APIs. It also introduces a number of notable improvements for developers:

* Arrays now support a new `length` property.
* Introduces support for `java.lang.Record` through the new `RecordELResolver`, which is enabled by default.
* Adds support for `java.lang.Optional` via the new `OptionalELResolver`, which is disabled by default.

