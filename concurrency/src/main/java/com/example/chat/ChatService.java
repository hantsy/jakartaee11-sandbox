package com.example.chat;

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
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.Sse;
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
