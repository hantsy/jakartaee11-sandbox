package com.example.intercept;

import jakarta.annotation.Priority;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InterceptorBinding;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE, ElementType.METHOD})
@InterceptorBinding
@Priority(Interceptor.Priority.APPLICATION+10)
public @interface Log {
}
