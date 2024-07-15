package com.example;


import jakarta.enterprise.concurrent.ContextServiceDefinition;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorDefinition;
import jakarta.enterprise.context.ApplicationScoped;

import static jakarta.enterprise.concurrent.ContextServiceDefinition.APPLICATION;
import static jakarta.enterprise.concurrent.ContextServiceDefinition.SECURITY;

@ManagedScheduledExecutorDefinition(
        name = "java:comp/MyScheduleExecutor",
        maxAsync = 10,
        context = "java:comp/MyScheduleContextService",
        virtual = true,
        qualifiers = {MyQualifier.class}
)
@ContextServiceDefinition(
        name = "java:comp/MyScheduleContextService",
        propagated = {SECURITY, APPLICATION},
        qualifiers = {MyQualifier.class}
)
@ApplicationScoped
public class ScheduleConfig {
}
