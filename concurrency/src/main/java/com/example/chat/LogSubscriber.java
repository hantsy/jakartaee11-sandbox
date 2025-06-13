package com.example.chat;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.Flow;
import java.util.logging.Logger;

@ApplicationScoped
public class LogSubscriber implements Flow.Subscriber<Long> {
    private Logger LOGGER = Logger.getLogger(LogSubscriber.class.getName());
    final public static int MAX_REQUESTS = 2;

    Flow.Subscription subscription;
    int requestCount = 0;

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        LOGGER.info("onSubscribe:" + subscription);
        this.subscription = subscription;
        this.subscription.request(1);
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
        LOGGER.info("onComplete...");
    }
}
