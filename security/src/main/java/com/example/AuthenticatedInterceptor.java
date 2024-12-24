package com.example;

import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.security.enterprise.AuthenticationException;
import jakarta.security.enterprise.SecurityContext;

@Interceptor
@Authenticated
public class AuthenticatedInterceptor {

    @Inject
    SecurityContext securityContext;

    @AroundInvoke
    public Object checkAuthenticated(InvocationContext invocationContext) throws Exception {
        if (securityContext.getCallerPrincipal() == null) {
            throw new AuthenticationException("Unauthorized");
        }

        return invocationContext.proceed();
    }
}
