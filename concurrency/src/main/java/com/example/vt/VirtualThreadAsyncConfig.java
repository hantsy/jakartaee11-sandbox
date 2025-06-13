package com.example.vt;


import jakarta.enterprise.concurrent.ContextServiceDefinition;
import jakarta.enterprise.concurrent.ManagedExecutorDefinition;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorDefinition;
import jakarta.enterprise.concurrent.ManagedThreadFactoryDefinition;
import jakarta.enterprise.context.ApplicationScoped;

import static jakarta.enterprise.concurrent.ContextServiceDefinition.*;

@ManagedExecutorDefinition(
        name = "java:comp/vtExecutor",
        maxAsync = 10,
        context = "java:comp/vtContextService",
        virtual = true,
        qualifiers = {WithVirtualThread.class}
)
@ContextServiceDefinition(
        name = "java:comp/vtContextService",
        propagated = {SECURITY, APPLICATION},
        qualifiers = {WithVirtualThread.class}
)
@ManagedThreadFactoryDefinition(
        name = "java:comp/vtThreadFactory",
        context = "java:comp/vtContextService",
        qualifiers = {WithVirtualThread.class},
        virtual = true
)
@ManagedScheduledExecutorDefinition(
        name = "java:comp/vtScheduleExecutor",
        maxAsync = 10,
        context = "java:comp/vtContextService",
        virtual = true,
        qualifiers = {WithVirtualThread.class}
)
@ApplicationScoped
public class VirtualThreadAsyncConfig {
}
