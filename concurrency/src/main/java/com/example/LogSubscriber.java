package com.example;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.concurrent.Flow;
import java.util.logging.Logger;

@ApplicationScoped
public class LogSubscriber implements Flow.Subscriber<Long> {
    final public static int MAX_REQUESTS = 2;

    Flow.Subscription subscription;
    int requestCount = 0;

    @Inject
    Logger LOG;

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        LOG.info("subscription:" + subscription);
        this.subscription = subscription;
        this.subscription.request(1);
    }

    @Override
    public void onNext(Long item) {
        LOG.info("onNext:" + item);
        if (requestCount % MAX_REQUESTS == 0) {
            this.subscription.request(2);
        }

        requestCount++;
    }

    @Override
    public void onError(Throwable throwable) {
        LOG.info("onError:" + throwable.getMessage());
        this.subscription.cancel();
    }

    @Override
    public void onComplete() {
        LOG.info("onComplete...");
    }
}
