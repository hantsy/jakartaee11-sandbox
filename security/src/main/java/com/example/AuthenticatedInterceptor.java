package com.example;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.security.enterprise.AuthenticationException;
import jakarta.security.enterprise.SecurityContext;

import java.util.logging.Level;
import java.util.logging.Logger;

@Interceptor
//@Priority(Interceptor.Priority.PLATFORM_BEFORE+10)
@Authenticated
public class AuthenticatedInterceptor {
    private static final Logger LOGGER = Logger.getLogger(AuthenticatedInterceptor.class.getName());

    @Inject
    SecurityContext securityContext;

    @AroundInvoke
    public Object checkAuthenticated(InvocationContext invocationContext) throws Exception {
        LOGGER.log(Level.INFO, "check Authentication....");
        if (securityContext.getCallerPrincipal() == null) {
            LOGGER.log(Level.INFO, "Principal is unauthenticated!!!");
            throw new AuthenticationException("Unauthorized");
        }

        return invocationContext.proceed();
    }
}
