package com.example;

import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class GreetingService {
    private final static Logger LOGGER = Logger.getLogger(GreetingService.class.getName());

    @Inject
    @MyQualifier
    private ManagedExecutorService executor;

    public CompletableFuture<GreetingRecord> greetAsync(String name) {
        return executor.supplyAsync(() -> {
            LOGGER.log(Level.INFO, "thread name: " + Thread.currentThread().getName());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return new GreetingRecord(name, LocalDateTime.now());
        });
    }

}
