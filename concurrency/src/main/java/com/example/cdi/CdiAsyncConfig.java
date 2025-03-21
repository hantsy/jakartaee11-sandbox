package com.example.cdi;


import jakarta.enterprise.concurrent.ContextServiceDefinition;
import jakarta.enterprise.concurrent.ManagedExecutorDefinition;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorDefinition;
import jakarta.enterprise.concurrent.ManagedThreadFactoryDefinition;
import jakarta.enterprise.context.ApplicationScoped;

import static jakarta.enterprise.concurrent.ContextServiceDefinition.APPLICATION;
import static jakarta.enterprise.concurrent.ContextServiceDefinition.SECURITY;

@ManagedExecutorDefinition(
        name = "java:comp/cdiExecutor",
        maxAsync = 10,
        context = "java:comp/cdiContextService",
        virtual = true,
        qualifiers = {CustomQualifier.class}
)
@ContextServiceDefinition(
        name = "java:comp/cdiContextService",
        propagated = {SECURITY, APPLICATION},
        qualifiers = {CustomQualifier.class}
)
@ManagedThreadFactoryDefinition(
        name = "java:comp/cdiThreadFactory",
        context = "java:comp/cdiContextService",
        qualifiers = {CustomQualifier.class},
        virtual = true
)
@ManagedScheduledExecutorDefinition(
        name = "java:comp/cdiScheduleExecutor",
        maxAsync = 10,
        context = "java:comp/cdiContextService",
        virtual = true,
        qualifiers = {CustomQualifier.class}
)
@ApplicationScoped
public class CdiAsyncConfig {
}
