package com.chenenyu.router.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for injected params.
 * <p>
 * Created by chenenyu on 2017/6/12.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.CLASS)
public @interface InjectParam {
    /**
     * Map param field with the specify key in extras.
     */
    String key() default "";
}
