package com.example.vt;

import jakarta.inject.Qualifier;

import java.lang.annotation.*;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE})
public @interface WithVirtualThread {
}
