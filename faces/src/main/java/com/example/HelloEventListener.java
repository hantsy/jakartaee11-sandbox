package com.example;

import jakarta.enterprise.context.*;
import jakarta.enterprise.event.Observes;

import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class HelloEventListener {
    private final static Logger LOGGER = Logger.getLogger(HelloEventListener.class.getName());

    public void observeInitialized(@Observes @Initialized(RequestScoped.class) Object hello) {
        LOGGER.log(Level.INFO, "observes event:: Initialized: {0}", new Object[]{hello});
    }

    public void observeBeforeDestroyed(@Observes @BeforeDestroyed(RequestScoped.class) Object hello) {
        LOGGER.log(Level.INFO, "observes event:: BeforeDestroyed: {0}", new Object[]{hello});
    }

    public void observeDestroyed(@Observes @Destroyed(RequestScoped.class) Object hello) {
        LOGGER.log(Level.INFO, "observes event:: Destroyed: {0}", new Object[]{hello});
    }
}
