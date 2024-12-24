package com.example;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.security.enterprise.AuthenticationException;
import jakarta.security.enterprise.SecurityContext;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

@Interceptor
//@Priority(Interceptor.Priority.PLATFORM_BEFORE+10)
@Authorized
public class AuthorizedInterceptor {
    private static final Logger LOGGER = Logger.getLogger(AuthorizedInterceptor.class.getName());

    @Inject
    SecurityContext securityContext;

    @AroundInvoke
    public Object checkAuthorized(InvocationContext invocationContext) throws Exception {
        LOGGER.log(Level.INFO, "check Authorization....");
        var methodAnnotation = invocationContext.getMethod().getAnnotation(Authorized.class);
        var authorizedAnnotation = methodAnnotation != null
                ? methodAnnotation
                : invocationContext.getClass().getAnnotation(Authorized.class);
        if (authorizedAnnotation != null) {
            if (Arrays.stream(authorizedAnnotation.roles()).anyMatch(role -> securityContext.isCallerInRole(role))) {
                return invocationContext.proceed();
            }

            LOGGER.log(Level.INFO, "Authorization failed!!!");
            throw new AuthenticationException("Forbidden");
        }
        return invocationContext.proceed();
    }
}
