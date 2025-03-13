package library.common;

import jakarta.interceptor.InterceptorBinding;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@InterceptorBinding
public @interface Logged {
}
