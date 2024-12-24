package com.example.schedule;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class Invite {
    private static final Logger LOGGER = Logger.getLogger(Invite.class.getName());
    private final List<String> names = new CopyOnWriteArrayList<>();

    public void send(String name, String email) {
        LOGGER.log(Level.INFO, "sending invite to:[{0}] via {1}", new Object[]{name, email});
        this.names.add(name);
    }

    public List<String> getNames() {
        return Collections.unmodifiableList(names);
    }

}
