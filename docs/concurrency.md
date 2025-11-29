# What's New in Jakarta Concurrency 3.1?

[Jakarta Concurrency 3.1](https://jakarta.ee/specifications/concurrency/3.1/) brings several new features into the Jakarta EE world:

* Integration with Java 21 Virtual thread
* CDI alignment with injecting Concurrency resources
* New `@Schedule` annotation
* Java 9 Flow/ReactiveStreams support

We have introduced the **Virtual Thread** support in a previous post - [Virtual Thread Support in Jakarta EE 11](./vt.md), and also demonstrated how to define the Concurrency resources with CDI `@Quaulifer` and inject them like regular CDI beans.

Here, we will skip these in this post and focus on the new `@Schedule` annotation and Reactive Streams support.

## New @Schedule Annotation

The legacy task scheduling was heavily bound to the EJB container, and [replacing EJB with Concurrency](https://github.com/jakartaee/concurrency/issues/252) and CDI-compatible facilities is a long-awatied mission. 

The purpose of new `@Schedule` annotation to replace the EJB one.

Let's create a simple example to show how to use it. 

Assume we need to notify the team members to attend a project meeting, here we create a bean with `@Schedule` to archive the purpose.

```java
@ApplicationScoped
public class StandUpMeeting {
    private final static Logger LOGGER = Logger.getLogger(StandUpMeeting.class.getName());

    private final static Map<String, String> members = Map.of(
            "jack", "jack@example.com",
            "ross", "ross@example.com"
    );

    @Inject
    ManagedThreadFactory threadFactory;

    @Inject
    NotificationService notificationService;

    @PostConstruct
    public void init() {
        LOGGER.log(Level.ALL, "init from scheduled tasks....");
    }

    @Asynchronous(
            //executor = "java:comp/MyScheduleExecutor", // can not refer by Qualifier???
            runAt = {
                    @Schedule(
                            daysOfWeek = {
                                    DayOfWeek.MONDAY,
                                    DayOfWeek.TUESDAY,
                                    DayOfWeek.WEDNESDAY,
                                    DayOfWeek.THURSDAY,
                                    DayOfWeek.FRIDAY
                            },
                            hours = 8
                    ), // daily standup
                    @Schedule(daysOfMonth = {1}, hours = {12}), // monthly meeting,
                    @Schedule(cron = "*/5 * * * * *") // every 5 seconds for test purpose
            }
    )
    void sendInviteNotifications() {
        LOGGER.log(Level.ALL, "running scheduled tasks....");
        try (ForkJoinPool pool = new ForkJoinPool(
                Runtime.getRuntime().availableProcessors(),
                threadFactory,
                (t, e) -> LOGGER.log(Level.INFO, "Thread: {0}, error: {1}", new Object[]{t.getName(), e.getMessage()}),
                true
        )) {

            var callables = members.keySet().stream()
                    .map(
                            name -> (Callable<Void>) () -> {
                                LOGGER.info("calling invite:" + name);
                                notificationService.send(name, members.get(name));
                                return null;
                            }
                    )
                    .toList();

            var futures = pool.invokeAll(callables)
                    .stream()
                    .map(
                            r -> {
                                try {
                                    return CompletableFuture.completedFuture(r.get(100, TimeUnit.MILLISECONDS));
                                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                                    throw new CompletionException(e);
                                }
                            }
                    )
                    .toList();

            var result = CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
            result.join();
        }
    }
}
```

As you see, the new `@Schedule` is located in the existing `@Asynchronous(runAt=...)`, which make the usage looks a little weird. 

The `NotificationService` is a dummy notification service used for test purpose.

```java
@ApplicationScoped
public class NotificationService {
    private static final Logger LOGGER = Logger.getLogger(NotificationService.class.getName());
    private final List<String> names = new CopyOnWriteArrayList<>();

    public void send(String name, String email) {
        LOGGER.log(Level.INFO, "sending invite to:[{0}] via {1}", new Object[]{name, email});
        this.names.add(name);
    }

    public List<String> getNames() {
        return Collections.unmodifiableList(names);
    }

}
```

Create a REST resource to trigger the scheduled tasks.

```java
@RequestScoped
@Path("schedule")
public class ScheduleResources {

    @Inject
    NotificationService notificationService;

    @Inject
    StandUpMeeting meeting;

    @POST
    public Response invite() {
        meeting.sendInviteNotifications();
        return Response.ok().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getInvitedNames() {
        return notificationService.getNames();
    }
}
```

After the application is deployed, we can start send notifications via endpoint `POST /schedule`, and check the invited names via `GET /schedule`. 

You can check the testing code [ScheduleTest](https://github.com/hantsy/jakartaee11-sandbox/blob/master/concurrency/src/test/java/com/example/it/ScheduleTest.java) to explore the progress. 

Unfornately, the new `@Schedule` annoation looks a little weird for developers. 
* It requirs an external invocation to trigger the scheduled tasks, can not start automaticially.
* It works as a nested `runAt` attribute of the existing `@Asynchronous` annoation.
* The attributes of `@Schedule` does not align with the modern equvilent ones in Quarkus and Spring.
* The is no equilvence of the legacy `@TimeoutAround` to handle the original schedule timeout.

I hope there is a completely new  replacement of the EJB `@Schedule`, and adopt the good part of the community's work, eg. Quarkus and Spring. 
Check the [proposal of the top-level standalone `@Scheduled` annotation](https://github.com/jakartaee/concurrency/issues/684).

## ReactiveStreams Support

