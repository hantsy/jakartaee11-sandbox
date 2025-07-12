package com.example.intercept;


import jakarta.enterprise.context.ApplicationScoped;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class Greeter {

    private static final Logger LOG = Logger.getLogger(Greeter.class.getName());


    @Timed
    @Log
    public void sayHello(String name) throws InterruptedException {
        var message = "Hello, " + name + "!";
        Thread.sleep(new Random().nextInt(10_000));
        LOG.log(Level.INFO, "building message:{0}", new Object[]{message});
    }
}
