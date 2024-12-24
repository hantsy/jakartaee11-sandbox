package com.example;

import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.security.enterprise.AuthenticationException;
import jakarta.security.enterprise.SecurityContext;

import java.util.Arrays;

@Interceptor
@Authorized
public class AuthorizedInterceptor {

    @Inject
    SecurityContext securityContext;

    @AroundInvoke
    public Object checkAuthorized(InvocationContext invocationContext) throws Exception {
        var methodAnnotation = invocationContext.getMethod().getAnnotation(Authorized.class);
        if (methodAnnotation != null) {
            if (Arrays.stream(methodAnnotation.roles()).anyMatch(role -> securityContext.isCallerInRole(role))) {
                return invocationContext.proceed();
            }

            throw new AuthenticationException("Forbidden");
        }
        var classAnnotation = invocationContext.getClass().getAnnotation(Authorized.class);
        if (classAnnotation != null) {
            if (Arrays.stream(classAnnotation.roles()).anyMatch(role -> securityContext.isCallerInRole(role))) {
                return invocationContext.proceed();
            }

            throw new AuthenticationException("Forbidden");
        }
        return invocationContext.proceed();
    }
}
