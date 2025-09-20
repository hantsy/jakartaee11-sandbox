# What's New in Jakarta Security 4.0

Jakarta Security provides user-friendly APIs for managing authentication and authorization in Jakarta EE applications.

Version 4.0 brings several notable enhancements, including:

* A standardized in-memory `IdentityStore`
* CDI qualifiers for built-in authentication mechanisms
* A basic handler for processing multiple authentication mechanisms

## In-Memory IdentityStore

While previous versions of the Jakarta Security implementation — [Eclipse Soteria](https://github.com/eclipse-ee4j/soteria) already included an in-memory identity store, version 4.0 officially standardizes this feature as part of the Jakarta Security specification.

Here’s an example of how to use the in-memory IdentityStore:

```java
@InMemoryIdentityStoreDefinition(
    value = {
        @Credentials(callerName = "admin", password = "password", groups = {"web", "rest"}),
        @Credentials(callerName = "webuser", password = "password", groups = {"web"}),
        @Credentials(callerName = "restuser", password = "password", groups = {"rest"})
    }
)
@DeclareRoles({"web", "rest"})
@ApplicationScoped
public class SecurityConfig {
}
```

In the example above, two roles: `web` and `rest` are defined, along with three users: `admin`, `webuser`, and `restuser`. All credentials are stored in memory.

> [!NOTE]
> The in-memory IdentityStore is intended for testing purposes, as it stores user credentials in memory. For production environments, it is recommended to use a database-backed IdentityStore to persist user credentials securely.

## CDI Qualifiers for Built-in Authentication Mechanisms

To better integrate with CDI, Jakarta Security 4.0 introduces a `qualifiers` attribute to the built-in `XXXAuthenticationMechanism` annotations. This allows developers to identify authentication mechanism resources as CDI beans and inject them using custom qualifiers.

For example, suppose you want to qualify the basic authentication mechanism and a custom form-based authentication mechanism with your own qualifiers.

First of all, create two custom Qualifiers - `WebAuthenticationQualifier` and `RestAuthenticationQualifier`.

```java
// WebAuthenticationQualifier.java
@Documented
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface WebAuthenticationQualifier {
}

// RestAuthenticationQualifier.java
@Documented
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface RestAuthenticationQualifier {
}
```

Next, in your `SecurityConfig` class, declare both `@BasicAuthenticationMechanismDefinition` and `@CustomFormAuthenticationMechanismDefinition`, setting the `qualifiers` attribute to the custom qualifiers you defined:

```java
@BasicAuthenticationMechanismDefinition(
    realmName = "BasicAuth",
    qualifiers = {RestAuthenticationQualifier.class}
)
@CustomFormAuthenticationMechanismDefinition(
    loginToContinue = @LoginToContinue(
        loginPage = "/login.faces",
        errorPage = "/login.faces?error",
        useForwardToLogin = false // use redirect
    ),
    qualifiers = {WebAuthenticationQualifier.class}
)
// ...
@ApplicationScoped
public class SecurityConfig {
}
```

Now, in your CDI beans, you can inject the declared `XXXAuthenticationMechanism` as qualified CDI beans:

```java
@ApplicationScoped
public class MultipleHttpAuthenticationMechanismHandler implements HttpAuthenticationMechanismHandler {
    @Inject
    @RestAuthenticationQualifier
    private HttpAuthenticationMechanism restAuthenticationMechanism;

    @Inject
    @WebAuthenticationQualifier
    private HttpAuthenticationMechanism webAuthenticationMechanism;
    // ...
}
```

