package com.example;

import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.security.enterprise.AuthenticationException;
import jakarta.security.enterprise.SecurityContext;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

@Interceptor
//@Priority(Interceptor.Priority.APPLICATION + 1)
@Authenticated
public class AuthenticatedInterceptor {
    private static final Logger LOGGER = Logger.getLogger(AuthenticatedInterceptor.class.getName());

    @Inject
    SecurityContext securityContext;

    @AroundInvoke
    public Object checkAuthenticated(InvocationContext ctx) throws Exception {
        LOGGER.log(Level.INFO, "Enter AuthenticatedInterceptor....");

        Method method = ctx.getMethod();
        Class<?> declaringClass = method.getDeclaringClass();
        LOGGER.log(Level.INFO, "Intercepting: {0}, {1}, {2}", new Object[]{declaringClass.getName(), method.getName(), ctx.getParameters()});

        var methodAnnotation = method.getAnnotation(Authenticated.class);
        LOGGER.log(Level.INFO, "Authenticated exists on method: {0},{1}", new Object[]{method.getName(), methodAnnotation});

        var authenticatedAnnotation = methodAnnotation != null ? methodAnnotation : declaringClass.getAnnotation(Authenticated.class);
        LOGGER.log(Level.INFO, "Authenticated exists on class: {0},{1}", new Object[]{declaringClass.getName(), authenticatedAnnotation});
        LOGGER.log(Level.INFO, "Check security context principal: {0}", new Object[]{securityContext.getCallerPrincipal()});

        if (authenticatedAnnotation != null && securityContext.getCallerPrincipal() == null) {
            LOGGER.log(Level.INFO, "Principal is unauthenticated!!!");
            throw new UnauthorizedException();
        }

        return ctx.proceed();
    }
}
