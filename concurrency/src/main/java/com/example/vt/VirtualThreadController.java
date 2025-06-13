package com.example.vt;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

@RequestScoped
@Path("vt")
public class VirtualThreadController {
    private final Logger LOGGER = Logger.getLogger(VirtualThreadController.class.getName());

    @Inject
    @WithVirtualThread
    ThreadFactory vtThreadFactory;

    @GET
    @Path("")
    public Response vtGet() {
        vtThreadFactory.newThread(new Thread(() -> {
            try {
                LOGGER.info("thread name: " + Thread.currentThread().getName());
                Thread.sleep(1_000);
                LOGGER.log(Level.ALL, "VT started");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }));

        return Response.accepted().build();
    }
}
