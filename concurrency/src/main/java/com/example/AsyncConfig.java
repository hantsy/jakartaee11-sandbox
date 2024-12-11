package com.example;


import jakarta.enterprise.concurrent.ContextServiceDefinition;
import jakarta.enterprise.concurrent.ManagedExecutorDefinition;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorDefinition;
import jakarta.enterprise.concurrent.ManagedThreadFactoryDefinition;
import jakarta.enterprise.context.ApplicationScoped;

import static jakarta.enterprise.concurrent.ContextServiceDefinition.APPLICATION;
import static jakarta.enterprise.concurrent.ContextServiceDefinition.SECURITY;

@ManagedExecutorDefinition(
        name = "java:comp/MyExecutor",
        maxAsync = 10,
        context = "java:comp/MyContextService",
        virtual = true,
        qualifiers = {MyQualifier.class}
)
@ContextServiceDefinition(
        name = "java:comp/MyContextService",
        propagated = {SECURITY, APPLICATION},
        qualifiers = {MyQualifier.class}
)
@ManagedThreadFactoryDefinition(
        name = "java:comp/MyThreadFactory",
        context = "java:comp/MyContextService",
        qualifiers = {MyQualifier.class},
        virtual = true
)
@ManagedScheduledExecutorDefinition(
        name = "java:comp/MyScheduleExecutor",
        maxAsync = 10,
        context = "java:comp/MyContextService",
        virtual = true,
        qualifiers = {MyQualifier.class}
)
@ApplicationScoped
public class AsyncConfig {
}
