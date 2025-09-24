# What's New in Jakarta Security 4.0
The Jakarta Security specification provides a collection of user-friendly APIs for handling authentication and authorization in Jakarta EE applications.
The Jakarta Security is based on Jakarta Authentication and Jakarta Authorization, but provides more control for developers.

Version 4.0 brings several notable improvements, including:

* A standardized in-memory `IdentityStore`
* CDI qualifiers for built-in authentication mechanisms
* A handler for processing multiple authentication mechanisms

## In-Memory IdentityStore

Earlier versions of the Jakarta Security implementation—[Eclipse Soteria](https://github.com/eclipse-ee4j/soteria) already included an in-memory identity store. With version 4.0, this feature is now officially standardized as part of the Jakarta Security specification.

Here’s how you can use the in-memory `IdentityStore`:

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

In this example, two roles - `web` and `rest` are defined, along with three users: `admin`, `webuser`, and `restuser`. All credentials are stored in memory.

> [!NOTE]
> The in-memory `IdentityStore` is intended for testing only, as it keeps user credentials in memory. For production, it’s recommended to use a database-backed `IdentityStore` to securely persist user credentials.

## CDI Qualifiers for Built-in Authentication Mechanisms

To enhance CDI integration, Jakarta Security 4.0 introduces a `qualifiers` attribute to the built-in `XXXAuthenticationMechanismDefinition` annotations. This allows you to identify authentication mechanism resources as CDI beans and inject them using custom qualifiers.

For example, if you want to use both the basic authentication mechanism and a custom form-based authentication mechanism in your CDI beans, start by creating two custom qualifiers: `WebAuthenticationQualifier` and `RestAuthenticationQualifier`.

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

Next, declare both `@BasicAuthenticationMechanismDefinition` and `@CustomFormAuthenticationMechanismDefinition` in your `SecurityConfig` class, setting the `qualifiers` attribute to the custom qualifiers you defined:

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

Now, you can inject the declared `XXXAuthenticationMechanism` as qualified CDI beans in your CDI beans:

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

## Handling Multiple Authentication Mechanisms

Previously, combining different authentication mechanisms in a Jakarta EE application was challenging using the Jakarta Security APIs. Version 4.0 introduces a new API, `HttpAuthenticationMechanismHandler`, which lets you handle incoming requests more flexibly.

```java
@Alternative
@Priority(APPLICATION)
@ApplicationScoped
public class MultipleHttpAuthenticationMechanismHandler implements HttpAuthenticationMechanismHandler {
    private static final Logger LOGGER = Logger.getLogger(MultipleHttpAuthenticationMechanismHandler.class.getName());

    @Inject
    @RestAuthenticationQualifier
    private HttpAuthenticationMechanism restAuthenticationMechanism;

    @Inject
    @WebAuthenticationQualifier
    private HttpAuthenticationMechanism webAuthenticationMechanism;

    @Override
    public AuthenticationStatus validateRequest(HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext) throws AuthenticationException {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        LOGGER.log(Level.INFO, "Request path (without context path): {0}", path);
        if (path.startsWith("/api")) {
            LOGGER.log(Level.INFO, "Handling authentication using RestAuthenticationQualifier HttpAuthenticationMechanism...");
            return restAuthenticationMechanism.validateRequest(request, response, httpMessageContext);
        }

        LOGGER.log(Level.INFO, "Handling authentication using WebAuthenticationQualifier HttpAuthenticationMechanism...");
        return webAuthenticationMechanism.validateRequest(request, response, httpMessageContext);
    }
}
```

In this example, authentication handling is delegated to the injected `HttpAuthenticationMechanism` instances. Basic authentication is applied to URIs starting with `/api`, while form-based authentication is used for other web pages.

The `@Alternative` annotation indicates that this bean is an alternative of the built-in bean `HttpAuthenticationMechanismHandler` provided by the Jakarta EE container, and can replace the existing one at runtime.

To activate the `MultipleHttpAuthenticationMechanismHandler` bean at runtime, you can use the `@Priority(APPLICATION)` annotation as shown above, or configure it in the CDI *beans.xml* file as follow:

```xml
<beans ...>
    <!-- ... -->
    <alternatives>
        <class>com.example.MultipleHttpAuthenticationMechanismHandler</class>
    </alternatives>
</beans>
```

## Example Project

The example project demonstrates all the samples covered in this article and also includes additional code snippets, such as `@Authenticated` and `@Authorized(roles)`, to illustrate real-world class and method level security protection.

To build and run the example project on GlassFish, use the following command:

```bash
mvn clean package cargo:run -Pglassfish
```

To test the web pages, open your browser and navigate to [http://localhost:8080/security-examples/test-servlet](http://localhost:8080/security-examples/test-servlet).

You’ll be redirected to the `/login` page. After logging in with either `webuser` or `admin` (as defined earlier), you’ll be taken back to the protected page.

To verify that REST API protection works as expected, open a terminal and run:

```bash
curl -X GET http://localhost:8080/security-examples/api/hello
```

You should receive a `401 Unauthorized` error.

Then, try with the predefined `restuser/password` credentials:

```bash
curl -X GET http://localhost:8080/security-examples/api/hello -u "restuser:password"
```

You should see a `200` status code and the response from the `/hello` endpoint.

The example project also includes [a simple test written in JUnit 5 and Arquillian](https://github.com/hantsy/jakartaee11-sandbox/blob/master/security/src/test/java/com/example/it/SecurityTest.java).

Open a terminal and run the following command to execute the test:

```bash 
mvn clean verify -P"arq-glassfish-managed"
```

You can find [the complete example project](https://github.com/hantsy/jakartaee11-sandbox/blob/master/security) on my GitHub and explore the code locally.
