package com.chenenyu.router.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Created by Cheney on 2017/3/6.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Interceptor {
    /**
     * Interceptor name.
     */
    String value();
}
