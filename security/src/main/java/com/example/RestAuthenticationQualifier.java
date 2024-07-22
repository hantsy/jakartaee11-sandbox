package com.example;

import jakarta.inject.Qualifier;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface RestAuthenticationQualifier {
}
