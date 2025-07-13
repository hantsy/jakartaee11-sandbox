package com.example.intercept;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

@Interceptor
@Log
public class LogInterceptor {
    private final static Logger LOG = Logger.getLogger(LogInterceptor.class.getName());

    @AroundInvoke
    public Object onLogged(InvocationContext ctx) {
        var method = ctx.getMethod();
        var declaringClass = method.getDeclaringClass();
        LOG.info("entering " + declaringClass.getSimpleName() + "." + method.getName() + " with parameters: " + Arrays.toString(ctx.getParameters()));
        LOG.log(Level.INFO, "intercept bindings: {0}",
                new Object[]{
                        ctx.getInterceptorBindings().stream().map(annotation ->annotation.annotationType().getSimpleName()).toList()
                });
        LOG.log(Level.INFO, "intercept bindings for Timed: {0}",
                new Object[]{
                        ctx.getInterceptorBindings(Timed.class).stream().map(annotation -> annotation.annotationType().getSimpleName()).toList()
                });
        try {
            var result = ctx.proceed();

            LOG.info("exiting " + declaringClass.getSimpleName() + "." + method.getName() + " with result: " + result);
            return result;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "exception in " + declaringClass.getSimpleName() + "." + method.getName(), e);
            throw new RuntimeException(e);
        }
    }
}
