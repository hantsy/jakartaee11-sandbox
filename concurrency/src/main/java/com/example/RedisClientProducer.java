package com.example;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;

import java.util.logging.Logger;

@ApplicationScoped
public class RedisClientProducer {
    private static final Logger LOGGER = Logger.getLogger(RedisClientProducer.class.getName());

    // Producer method for RedisClient
    @Produces
    @ApplicationScoped
    public RedisClient createRedisClient() {
        return RedisClient.create("redis://localhost:6379");
    }

    // Disposer method to close the RedisClient
    public void closeRedisClient(@Disposes RedisClient redisClient) {
        LOGGER.finest("shutdown redis client...");
        redisClient.shutdown();
    }

    @Produces
    @ApplicationScoped
    public StatefulRedisConnection<String, String> redisConnection(RedisClient redisClient) {
        return redisClient.connect();
    }

    public void closeConnection(@Disposes StatefulRedisConnection<String, String> redisConnection) {
        LOGGER.finest("closing redis connection...");
        redisConnection.close();
    }
}
