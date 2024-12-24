package com.example;

import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

@Provider
public class JsonbContextResolver implements ContextResolver<Jsonb> {

    @Inject
    Jsonb jsonb;

    @Override
    public Jsonb getContext(Class<?> type) {

        return jsonb;
//        JsonbConfig config = new JsonbConfig()
//                .withPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CASE_WITH_UNDERSCORES)
//                .withFormatting(true)
//                .withNullValues(false);
//        return JsonbBuilder.newBuilder().withConfig(config).build();
    }
}
