# Virtual Thread Support in Jakarta EE 11

Java 21 introduces Virtual Threads, a lightweight threading solution that allows applications to create a large number of concurrent threads with minimal memory consumption. Unlike Platform Threads, which are resource-intensive and heavily dependent on system processor cores, Virtual Threads are designed to be efficient and scalable. This new feature significantly enhances the concurrency capabilities of Java applications, making it easier to handle high-throughput workloads with improved performance and reduced overhead.

Jakarta EE 11 sets Java 17 as the baseline but it also requires all implementations to support Java 21. To align with Java 21, Jakarta Concurrency 3.1 introduces support for virtual threads, enhancing the concurrency capabilities of Jakarta EE applications.

In Jakarta Concurrency 3.1, a new attribute `virtual` is added to existing annotations such as `@ManagedExecutorDefinition`, `@ManagedScheduledExecutorDefinition`, and `@ManagedThreadFactoryDefinition`. This attribute specifies whether to create the managed resources using virtual threads. By default, the virtual thread support is not enabled, the `virtual` attribute is `false` implicitly. To enable it, explicitly set the value of the `virtual` attribute to `true`.

> [!NOTE]
> To leverage Virtual Threads in Jakarta EE, ensure your applications are running on a Jakarta EE 11 implementation with a Java 21+ runtime.

For example, declare virtual thread-based resources:

```java
@ManagedExecutorDefinition(
    name = "java:comp/vtExecutor",
    maxAsync = 10,
    context = "java:comp/vtContextService",
    virtual = true
)
@ContextServiceDefinition(
    name = "java:comp/vtContextService",
    propagated = {SECURITY, APPLICATION}
)
@ManagedThreadFactoryDefinition(
    name = "java:comp/vtThreadFactory",
    context = "java:comp/vtContextService",
    virtual = true
)
@ManagedScheduledExecutorDefinition(
    name = "java:comp/vtScheduleExecutor",
    context = "java:comp/vtContextService",
    maxAsync = 10,
    virtual = true
)
@ApplicationScoped
public class VirtualThreadAsyncConfig {
}
```

To utilize these resources within your CDI beans, simply inject them using the `@Resource` annotation with their respective JNDI names:

```java
@Resource(lookup = "java:comp/vtExecutor")
ManagedExecutorService executor;

@Resource(lookup = "java:comp/vtContextService")
ContextService contextService;

@Resource(lookup = "java:comp/vtThreadFactory")
ManagedThreadFactory threadFactory;

@Resource(lookup = "java:comp/vtScheduleExecutor")
ManagedScheduledExecutorService scheduledExecutor;
```

Alternatively, Jakarta Concurrency 3.1 allows managed resources to be injected using CDI `@Inject`. To differentiate the above virtual thread aware managed resources from the built-in managed resources, you can use custom CDI `@Qualifier` annotations to qualify them. When specifying the `qualifiers` attribute on the resource annotations, these resources can be injected with type-safe `@Qualifer' in CDI beans like other normal CDI beans. 

For example, create a custom `@Qualifier` annotation:

```java
@Qualifier
@Retention(RUNTIME)
@Target({METHOD, FIELD, PARAMETER, TYPE})
public @interface WithVirtualThread {
}
```

Next, add the `qualifiers` attribute to the resource annotations and set its value to the created `@Qualifier` class.

```java
@ManagedExecutorDefinition(
    //...
    qualifiers = {WithVirtualThread.class}
)
@ContextServiceDefinition(
       //...
    qualifiers = {WithVirtualThread.class}
)
@ManagedThreadFactoryDefinition(
    //...
    qualifiers = {WithVirtualThread.class}
)
@ManagedScheduledExecutorDefinition(
    //...
    qualifiers = {WithVirtualThread.class}
)
@ApplicationScoped
public class VirtualThreadAsyncConfig {
}
```

Finally, inject these resources using the custom `@Qualifier` annotation:

```java
@Inject
@WithVirtualThread
ManagedExecutorService executor;

@Inject
@WithVirtualThread
ContextService contextService;

@Inject
@WithVirtualThread
ManagedThreadFactory threadFactory;

@Inject
@WithVirtualThread
ManagedScheduledExecutorService scheduledExecutor;
```

Here are some examples of using these resources to run asynchronous tasks in a Jakarta EE environment:

```java
executor.submit(() -> {
    // Task logic here
    System.out.println("Task executed using ManagedExecutorService");
});

scheduledExecutor.schedule(() -> {
    // Task logic here
    System.out.println("Task scheduled using ManagedScheduledExecutorService");
}, 10, TimeUnit.SECONDS);

try (ForkJoinPool pool = new ForkJoinPool(
    Runtime.getRuntime().availableProcessors(),
    threadFactory,
    (t, e) -> LOGGER.log(Level.INFO, "Thread: {0}, error: {1}", new Object[]{t.getName(), e.getMessage()}),
    true
)) {
    pool.submit/invokeAll...
}

try (var scope = new StructuredTaskScope("vt", threadFactory)) {
    Future<String> task1 = scope.fork(() -> {... });
    Future<Integer> task2 = scope.fork(() -> {... });

    scope.join(); // Wait for all tasks to complete
    scope.throwIfFailed(); // Propagate any task failure

    // handle the results
}
```

While virtual threads are not a silver bullet for all performance issues, they can significantly enhance overall application performance by increasing throughput in some scenarios, such as database calls, handling HTTP interactions, etc.
