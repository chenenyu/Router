package com.chenenyu.router.template;

import com.chenenyu.router.RouteInterceptor;

import java.util.Map;

/**
 * Interceptor table mapping.
 * <p>
 * Created by chenenyu on 2017/6/30.
 */
public interface InterceptorTable {
    /**
     * Mapping between name and interceptor.
     *
     * @param map name -> interceptor.
     */
    void handle(Map<String, Class<? extends RouteInterceptor>> map);
}
