package com.example.intercept;


import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import java.util.logging.Logger;

@Interceptor
@Timed
class TimedInterceptor {

    private final static Logger LOG = Logger.getLogger(TimedInterceptor.class.getName());

    @AroundInvoke
    public Object onTimed(InvocationContext ctx) throws Exception {
        var method = ctx.getMethod();
        var declaringClass = method.getDeclaringClass();
        var started = System.currentTimeMillis();
        LOG.info("entering " + declaringClass.getSimpleName() + "." + method.getName() + " at: " + started);
        try {
            var result = ctx.proceed();

            var end = System.currentTimeMillis();
            LOG.info("exiting " + declaringClass.getSimpleName() + "." + method.getName() + " at : " + end);
            LOG.info("it took: " + (end - started));
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
