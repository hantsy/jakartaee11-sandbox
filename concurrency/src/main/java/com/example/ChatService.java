package com.example;

import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import io.lettuce.core.api.sync.RedisCommands;
import jakarta.enterprise.concurrent.ContextService;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.SseEventSink;
import org.reactivestreams.FlowAdapters;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Flow;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class ChatService {
    @Inject
    @MyQualifier
    private ManagedExecutorService executor;

    @Inject
    @MyQualifier
    private ContextService contextService;

    @Inject
    StatefulRedisConnection<String, String> redisConnection;

    @Inject
    Jsonb jsonb;

    @Inject
    Logger LOG;

    @Inject
    LogSubscriber logSubscriber;

    @Inject
    Event<ChatMessage> chatMessageEvent;

    private final Map<String, SseRequest> requests = new ConcurrentHashMap<>();

    public void register(String id, SseRequest request) {
        LOG.log(Level.FINEST, "register request:{0}", id);
        requests.put(id, request);
    }

    public void deregister(String uuid) {
        LOG.log(Level.FINEST, "deregister request:{0}", uuid);
        SseRequest req = requests.remove(uuid);
        try (SseEventSink eventSink = req.sink()) {
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
                                contextService.contextualSubscriber(logSubscriber)
                        )
                );
    }

    public void onMessage(@Observes ChatMessage msg) {
        requests.values().forEach(
                req -> req.sink().send(
                        req.sse().newEventBuilder()
                                .mediaType(MediaType.APPLICATION_JSON_TYPE)
                                .id(UUID.randomUUID().toString())
                                .name("message from cdi")
                                .data(msg)
                                .build()
                )
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

}
