# What's New in Jakarta Concurrency 3.1?

Jakarta Concurrency provides a standard API for managing concurrent tasks in Jakarta EE applications. It exposes managed executor services, thread factories, and context propagation helpers so that concurrent work runs with container-managed concurrency resources.

[Jakarta Concurrency 3.1](https://jakarta.ee/specifications/concurrency/3.1/) introduces several notable improvements:

- Integration with Java 21 virtual threads
- Improved CDI support for injecting concurrency resources
- A new `@Schedule` annotation for task scheduling
- Java 9 Flow / Reactive Streams support

We covered virtual threads in an earlier post — [Virtual Thread Support in Jakarta EE 11](./vt.md) — and showed how to define concurrency resources with CDI `@Qualifier`s so they can be injected like regular CDI beans.

In this post, we'll skip those topics and focus on the new `@Schedule` annotation and the Reactive Streams support.

## New `@Schedule` Annotation

Legacy task scheduling has long been tied to the EJB container. Porting EJB functionalities to CDI-compatible APIs has been a long-standing effort ([see discussion](https://github.com/jakartaee/concurrency/issues/252)). The new [`@Schedule`](https://jakarta.ee/specifications/platform/11/apidocs/jakarta/enterprise/concurrent/schedule) annotation aims to replace the [EJB scheduling annotation](https://jakarta.ee/specifications/platform/11/apidocs/jakarta/ejb/schedule) and provide a more portable, CDI-friendly mechanism.

The example below demonstrates a simple usage of `@Schedule`.

Suppose we need to notify team members about a recurring project meeting. The bean below uses `@Schedule` to trigger those notifications.

```java
@ApplicationScoped
public class StandUpMeeting {
    private static final Logger LOGGER = Logger.getLogger(StandUpMeeting.class.getName());

    private static final Map<String, String> members = Map.of(
            "jack", "jack@example.com",
            "ross", "ross@example.com"
    );

    @Inject
    ManagedThreadFactory threadFactory;

    @Inject
    NotificationService notificationService;

    @PostConstruct
    public void init() {
        LOGGER.log(Level.ALL, "init from scheduled tasks...");
    }

    @Asynchronous(
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
                    @Schedule(daysOfMonth = {1}, hours = {12}), // monthly meeting
                    @Schedule(cron = "*/5 * * * * *") // every 5 seconds (test)
            }
    )
    void sendInviteNotifications() {
        LOGGER.log(Level.ALL, "running scheduled tasks...");
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

As you can see, the new `@Schedule` is currently declared inside the `@Asynchronous(runAt = ...)` attribute, which some developers find awkward.

The `NotificationService` below is a simple test service used in the example.

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

Create a REST resource that triggers the scheduled tasks:

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

After deployment, you can trigger notifications with `POST /schedule` and view invited names with `GET /schedule`.

See the test `ScheduleTest` for a runnable example: [ScheduleTest](https://github.com/hantsy/jakartaee11-sandbox/blob/master/concurrency/src/test/java/com/example/it/ScheduleTest.java).


Unfortunately, the current `@Schedule` design has a few rough edges:
- It requires an external invocation to trigger scheduled tasks. It can not start automatically. see: [jakartaee/concurrency#624](https://github.com/jakartaee/concurrency/issues/624)
- It is expressed as a nested `runAt` attribute inside `@Asynchronous`, which some find unintuitive.
- Its attributes are not aligned with modern equivalents in frameworks such as Quarkus and Spring.
- There is no clear replacement for the legacy timeout callback pattern for handling schedule timeouts.

A cleaner, top-level scheduling annotation that adopts community best practices would be preferable. See the proposal for a standalone `@Scheduled` annotation: https://github.com/jakartaee/concurrency/issues/684


## Reactive Streams Support

Jakarta Concurrency 3.1 adds first-class support for the [Java 9 `Flow`](https://docs.oracle.com/javase/9/docs/api/java/util/concurrent/Flow.html) ([Reactive Streams](https://www.reactive-streams.org/)) API, making it easier to build asynchronous, back-pressured pipelines that interoperate with other reactive libraries.

The `ContextService` contains two helper methods such as [`contextualSubscriber`](https://jakarta.ee/specifications/platform/11/apidocs/jakarta/enterprise/concurrent/contextservice#contextualSubscriber(java.util.concurrent.Flow.Subscriber)) and [`contextualProcessor`](https://jakarta.ee/specifications/platform/11/apidocs/jakarta/enterprise/concurrent/contextservice#contextualProcessor(java.util.concurrent.Flow.Processor)). They are used to wrap standard Flow `Subscriber` and `Processor` implementations so they execute with proper Jakarta EE context propagation (CDI, JTA, Security).

The example below demonstrates these concepts with a simple chat application that uses CDI events and Server-Sent Events (SSE) to publish messages and Redis as a backing store. A contextual subscriber is used to asynchronously process and count messages.

First, define the sample subscriber - `RequestCountSubscriber`:

```java
@ApplicationScoped
public class RequestCountSubscriber implements Flow.Subscriber<Long> {
    private Logger LOGGER = Logger.getLogger(RequestCountSubscriber.class.getName());
    final public static int MAX_REQUESTS = 2;

    Flow.Subscription subscription;
    int requestCount = 0;

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        LOGGER.info("onSubscribe:" + subscription);
        this.subscription = subscription;
        this.subscription.request(1);
        this.requestCount++;
    }

    @Override
    public void onNext(Long item) {
        LOGGER.info("onNext:" + item);
        if (requestCount % MAX_REQUESTS == 0) {
            this.subscription.request(MAX_REQUESTS);
        }
        requestCount++;
    }

    @Override
    public void onError(Throwable throwable) {
        LOGGER.info("onError:" + throwable.getMessage());
        this.subscription.cancel();
    }

    @Override
    public void onComplete() {
        LOGGER.log(Level.INFO, "onComplete: request count:{0}", new Object[]{this.requestCount});
    }
}
```

Next, create a REST resource to publish messages and subscribe the message via SSE:

```java
@ApplicationScoped
@Path("chat")
public class ChatResource {

    @Inject
    ChatService chatService;

    @Context
    Sse sse;

    @PostConstruct
    public void init() {
        chatService.setSse(this.sse);
    }

    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void join(@Context SseEventSink sink) {
        var userId = UUID.randomUUID();
        chatService.register(userId, sink);
    }

    @DELETE
    @Path("{id}")
    public void quit(@PathParam("id") UUID id) {
        chatService.deregister(id);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void send(@Valid NewMessageCommand message) {
        chatService.send(ChatMessage.of(message.body()));
    }

    @GET
    @Path("sync")
    @Produces(MediaType.APPLICATION_JSON)
    public Response latestMessages() {
        return Response.ok(chatService.latest10Messages()).build();
    }

    @GET
    @Path("async")
    @Produces(MediaType.APPLICATION_JSON)
    public Response latestMessagesAsync() {
        return Response.ok(chatService.latest10MessagesFuture()).build();
    }

    @GET
    @Path("flow")
    @Produces(MediaType.APPLICATION_JSON)
    public Flow.Publisher<ChatMessage> latestMessagesFlow() {
        return chatService.latest10MessagesFlowPublisher();
    }
}
```

Finally, implement the `ChatService` to handle incoming messages, store them in Redis, and use the contextual subscriber to subscribe to them. It is also responsible for sending SSE events to connected clients.    

```java
@ApplicationScoped
public class ChatService {
    @Inject
    private ManagedExecutorService executor;

    @Inject
    private ContextService contextService;

    @Inject
    StatefulRedisConnection<String, String> redisConnection;

    @Inject
    Jsonb jsonb;

    @Inject
    Logger LOG;

    @Inject
    RequestCountSubscriber requestCountSubscriber;

    @Inject
    Event<ChatMessage> chatMessageEvent;

    private Sse sse;

    private final Map<UUID, SseEventSink> sinks = new ConcurrentHashMap<>();

    public void register(UUID id, SseEventSink request) {
        LOG.log(Level.FINEST, "register request:{0}", id);
        sinks.put(id, request);
    }

    public void deregister(UUID uuid) {
        LOG.log(Level.FINEST, "deregister request:{0}", uuid);
        SseEventSink eventSink = sinks.remove(uuid);
        try {
            eventSink.close();
            LOG.log(Level.FINEST, "closing sink: {0}", eventSink);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            LOG.log(Level.ALL, "closed SSE event sink");
        }
    }

    public void send(ChatMessage message) {
        RedisReactiveCommands<String, String> commands = redisConnection.reactive();
        commands.lpush("chat", jsonb.toJson(message))
                .doOnSuccess(
                        inserted -> {
                            LOG.log(Level.FINEST, "inserted items into redis:" + inserted);
                            chatMessageEvent.fire(message);
                        }
                )
                .subscribe(
                        FlowAdapters.toSubscriber(
                                contextService.contextualSubscriber(requestCountSubscriber)
                        )
                );
    }

    public void onMessage(@Observes ChatMessage msg) {
        sinks.values()
                .forEach(sink -> {
                            OutboundSseEvent outboundSseEvent = this.sse.newEventBuilder()
                                    .mediaType(MediaType.APPLICATION_JSON_TYPE)
                                    .id(UUID.randomUUID().toString())
                                    .name("message from cdi")
                                    .data(msg)
                                    .build();
                            sink.send(outboundSseEvent);
                        }
                );
    }

    public List<ChatMessage> latest10Messages() {
        RedisCommands<String, String> commands = redisConnection.sync();

        return commands.lpop("chat", 10)
                .stream()
                .map(it -> jsonb.fromJson(it, ChatMessage.class))
                .toList();
    }

    public CompletableFuture<List<ChatMessage>> latest10MessagesFuture() {
        RedisAsyncCommands<String, String> commands = redisConnection.async();

        return commands.lpop("chat", 10)
                .thenApplyAsync(
                        msg -> msg.stream()
                                .map(it -> jsonb.fromJson(it, ChatMessage.class))
                                .toList(),
                        executor
                )
                .toCompletableFuture();
    }

    public Flow.Publisher<ChatMessage> latest10MessagesFlowPublisher() {
        RedisReactiveCommands<String, String> commands = redisConnection.reactive();

        Flux<ChatMessage> messageFlux = commands.lpop("chat", 10)
                .map(it -> jsonb.fromJson(it, ChatMessage.class))
                .subscribeOn(Schedulers.fromExecutor(executor));

        return FlowAdapters.toFlowPublisher(messageFlux);
    }

    public void setSse(Sse sse) {
        this.sse = sse;
    }
}
```

The RedisConnection bean is defined as follows:

```java
@ApplicationScoped
public class RedisClientProducer {
    private static final Logger LOGGER = Logger.getLogger(RedisClientProducer.class.getName());

    // Producer method for RedisClient
    @Produces
    @ApplicationScoped
    public RedisClient createRedisClient() {
        return RedisClient.create("redis://localhost:6379");
    }

    // Disposer method to close the RedisClient
    public void closeRedisClient(@Disposes RedisClient redisClient) {
        LOGGER.finest("shutdown redis client...");
        redisClient.shutdown();
    }

    @Produces
    @ApplicationScoped
    public StatefulRedisConnection<String, String> redisConnection(RedisClient redisClient) {
        return redisClient.connect();
    }

    public void closeConnection(@Disposes StatefulRedisConnection<String, String> redisConnection) {
        LOGGER.finest("closing redis connection...");
        redisConnection.close();
    }
}
```
The `NewMessageCommand` and `ChatMessage` classes are simple POJOs:

```java
public record NewMessageCommand(
        @NotBlank String body
) {
}
```

```java
public record ChatMessage(String body, LocalDateTime sentAt) {
    static ChatMessage of(String body) {
        return new ChatMessage(body, LocalDateTime.now());
    }
}
```

With this setup, messages sent to the chat service are stored in Redis and broadcast to connected clients via SSE. The `RequestCountSubscriber` processes messages asynchronously, demonstrating Jakarta Concurrency's Reactive Streams support.

After deployment, you can interact with the service using the REST endpoints: e.g., `GET /chat` to join a chat conversation and track the new messages via SSE, `POST /chat` to send new messages, and `GET /chat/sync` or `GET /chat/async` to retrieve the latest 10 messages.

> [!Warning]
> Jakarta REST does not yet provide native reactive-streams support, so `GET /chat/flow` may not work reliably on some application servers.

See the complete example in this test class: [ChatResourceTest](https://github.com/hantsy/jakartaee11-sandbox/blob/master/concurrency/src/test/java/com/example/it/ChatResourceTest.java).




