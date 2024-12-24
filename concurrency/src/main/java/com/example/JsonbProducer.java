package com.example;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.config.PropertyNamingStrategy;

@ApplicationScoped
public class JsonbProducer {

    @Produces
    @ApplicationScoped
    public Jsonb jsonb() {
        JsonbConfig config = new JsonbConfig()
                .withPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CASE_WITH_UNDERSCORES)
                .withFormatting(true)
                .withNullValues(false);
        return JsonbBuilder.newBuilder().withConfig(config).build();
    }
}
