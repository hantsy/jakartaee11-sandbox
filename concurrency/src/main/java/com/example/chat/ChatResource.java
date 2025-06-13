package com.example.chat;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;

import java.util.UUID;
import java.util.concurrent.Flow;

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

//    @GET
//    @Path("async")
//    @Produces(MediaType.APPLICATION_JSON)
//    public CompletionStage<Response> latestMessagesAsync() {
//        return chatService.latest10MessagesFuture()
//                .thenApplyAsync(chatMessage -> Response.ok(chatMessage).build());
//    }

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
