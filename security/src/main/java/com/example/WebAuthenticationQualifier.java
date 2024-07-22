package com.example;

import jakarta.inject.Qualifier;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface WebAuthenticationQualifier {
}
