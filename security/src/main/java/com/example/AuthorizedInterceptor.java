package com.example;

import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.security.enterprise.AuthenticationException;
import jakarta.security.enterprise.SecurityContext;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

@Interceptor
//@Priority(Interceptor.Priority.APPLICATION + 2)
@Authorized
public class AuthorizedInterceptor {
    private static final Logger LOGGER = Logger.getLogger(AuthorizedInterceptor.class.getName());

    @Inject
    SecurityContext securityContext;

    @AroundInvoke
    public Object checkAuthorized(InvocationContext ctx) throws Exception {
        LOGGER.log(Level.INFO, "check Authorization....");

        Method method = ctx.getMethod();
        Class<?> declaringClass = method.getDeclaringClass();
        LOGGER.log(Level.INFO, "Intercepting: {0}, {1}, {2}", new Object[]{declaringClass.getName(), method.getName(), ctx.getParameters()});

        var methodAnnotation = method.getAnnotation(Authorized.class);
        LOGGER.log(Level.INFO, "Authorized annotation exists on method: {0},{1}", new Object[]{method.getName(), methodAnnotation});

        var authorizedAnnotation = methodAnnotation != null ? methodAnnotation : declaringClass.getAnnotation(Authorized.class);
        LOGGER.log(Level.INFO, "Authorized annotation exists on class: {0},{1}", new Object[]{declaringClass.getName(), authorizedAnnotation});
        LOGGER.log(Level.INFO, "Check security context principal: {0}", new Object[]{securityContext.getCallerPrincipal()});

        if (authorizedAnnotation != null) {
            if (securityContext.getCallerPrincipal() == null) {
                LOGGER.log(Level.INFO, "Principal is unauthenticated!!!");
                throw new UnauthorizedException();
            }

            if (Arrays.stream(authorizedAnnotation.roles()).anyMatch(role -> securityContext.isCallerInRole(role))) {
                return ctx.proceed();
            }

            LOGGER.log(Level.INFO, "Authorization failed!!!");
            throw new ForbiddenException();
        }
        return ctx.proceed();
    }
}
