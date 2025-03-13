package library.common;


import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import java.util.logging.Level;
import java.util.logging.Logger;

@Logged
@Interceptor
public class LoggedInterceptor {
    private static final Logger logger = Logger.getLogger(LoggedInterceptor.class.getName());

    @AroundInvoke
    public Object logMethodCall(InvocationContext context) throws Exception {
        Object[] params = context.getParameters();
        String methodName = context.getMethod().getName();
        logger.log(Level.INFO, "Entering method: {0} with parameters: {1}", new Object[]{methodName, params});
        try {
            Object result = context.proceed();
            logger.log(Level.INFO, "Exiting method: {0} with result: {1}", new Object[]{methodName, result});
            return result;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception in method: {0}", new Object[]{e});
            throw e;
        }
    }
}
