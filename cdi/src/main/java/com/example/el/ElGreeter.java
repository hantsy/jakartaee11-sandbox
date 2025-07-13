package com.example.el;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@ApplicationScoped
@Named("elGreeter")
public class ElGreeter {

    public String hello(String name) {
        return "Hello " + name;
    }
}
