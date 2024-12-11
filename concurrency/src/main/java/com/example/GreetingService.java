package com.example;

import jakarta.enterprise.concurrent.Asynchronous;
import jakarta.enterprise.concurrent.ContextService;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class GreetingService {
    private final static Logger LOGGER = Logger.getLogger(GreetingService.class.getName());

    @Inject
    @MyQualifier
    private ManagedExecutorService executor;

    @Inject
    @MyQualifier
    private ContextService contextService;

    @Asynchronous
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

    public Flow.Publisher<GreetingRecord> greetFlow(String name) {
        class TransformProcessor<S, T> extends SubmissionPublisher<T> implements Flow.Processor<S, T> {
            final Function<? super S, ? extends T> function;
            Flow.Subscription subscription;

            TransformProcessor(Executor executor, int maxBufferCapacity, Function<? super S, ? extends T> function) {
                super(executor, maxBufferCapacity);
                this.function = function;
            }

            public void onSubscribe(Flow.Subscription subscription) {
                (this.subscription = subscription).request(1);
            }

            public void onNext(S item) {
                subscription.request(1);
                submit(function.apply(item));
            }

            public void onError(Throwable ex) {
                closeExceptionally(ex);
            }

            public void onComplete() {
                close();
            }
        }

        class LogPublisher extends SubmissionPublisher<GreetingRecord> {
            public LogPublisher(Executor executor, int maxBufferCapacity) {
                super(executor, maxBufferCapacity);
            }
        }

        var publisher = new LogPublisher(executor, 10);
        publisher.submit(new GreetingRecord("Flow", LocalDateTime.now()));

        return publisher;
    }

}
