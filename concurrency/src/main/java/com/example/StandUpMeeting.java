package com.example;

import jakarta.enterprise.concurrent.Asynchronous;
import jakarta.enterprise.concurrent.ManagedThreadFactory;
import jakarta.enterprise.concurrent.Schedule;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.DayOfWeek;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class StandUpMeeting {
    private final static Logger LOGGER = Logger.getLogger(StandUpMeeting.class.getName());

    private final static Map<String, String> members = Map.of(
            "jack", "jack@example.com",
            "ross", "ross@example.com"
    );

    @Inject
    @MyQualifier
    ManagedThreadFactory threadFactory;

    @Asynchronous(
            executor = "java:comp/MyScheduleExecutor", // can not refer by Qualifier???
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
                    @Schedule(daysOfMonth = {1}, hours = 12), // monthly meeting,
                    @Schedule(cron = "*/5 * * * *") // every 5 minutes for test purpose
            }
    )
    CompletableFuture<Void> inviteToMeeting() {

        ForkJoinPool pool = new ForkJoinPool(
                Runtime.getRuntime().availableProcessors(),
                threadFactory,
                (t, e) -> LOGGER.log(Level.INFO, "Thread: {0}, error: {1}", new Object[]{t.getName(), e.getMessage()}),
                true
        );

        java.util.List<Callable<Void>> callables = members.keySet().stream()
                .map(
                        name -> (Callable<Void>) () -> {
                            LOGGER.info("inviting " + name + '-' + members.get(name));
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
        pool.shutdown();
        return result;
    }
}
