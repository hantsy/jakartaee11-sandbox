package com.example.broadcast;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseBroadcaster;
import jakarta.ws.rs.sse.SseEventSink;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

// @Context constructor injection does not work with CDI Singleton/ApplicationScoped
// see: https://github.com/eclipse-ee4j/jersey/issues/5854
@ApplicationScoped
@Path("broadcast")
public class BroadcasterResource {
    private final Logger LOGGER = Logger.getLogger(BroadcasterResource.class.getName());

    private @Context Sse sse;
    private SseBroadcaster broadcaster;

    @PostConstruct
    public void init() {
        this.broadcaster = sse.newBroadcaster();
        this.broadcaster.onClose(sseEventSink -> {
            try {
                sseEventSink.close();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, e.getMessage(), e);
            }
        });
        this.broadcaster.onError((sseEventSink, throwable) -> {
            sseEventSink.send(sse.newEvent("error", throwable.getMessage()));
            try {
                sseEventSink.close();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, e.getMessage(), e);
            }
        });
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.TEXT_PLAIN)
    public String broadcastMessage(String message) {
        final OutboundSseEvent event = sse.newEventBuilder()
                .name("message")
                .mediaType(MediaType.TEXT_PLAIN_TYPE)
                .data(String.class, message)
                .build();

        broadcaster.broadcast(event);

        return "Message '" + message + "' has been broadcast.";
    }

    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void listenToBroadcast(@Context SseEventSink eventSink) {
        this.broadcaster.register(eventSink);
    }
}