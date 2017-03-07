package com.chenenyu.router.compiler;

/**
 * <p>
 * Created by Cheney on 2017/1/12.
 */
public class Consts {
    public static final String PACKAGE_NAME = "com.chenenyu.router";
    public static final String DOT = ".";
    public static final String ROUTE_TABLE = "RouteTable";
    public static final String ROUTE_TABLE_FULL_NAME = PACKAGE_NAME + DOT + ROUTE_TABLE;
    public static final String ACTIVITY_FULL_NAME = "android.app.Activity";
    /**
     * Used for reflection.
     */
    public static final String ROUTE_TABLE_METHOD_NAME = "handleActivityTable";

    public static final String OPTION_MODULE_NAME = "moduleName";

    public static final String ROUTE_ANNOTATION_TYPE = "com.chenenyu.router.annotation.Route";
    public static final String INTERCEPTOR_ANNOTATION_TYPE = "com.chenenyu.router.annotation.Interceptor";
    public static final String INTERCEPTOR_INTERFACE = PACKAGE_NAME + DOT + "RouteInterceptor";

    public static final String INTERCEPTORS = "Interceptors";
    /**
     * Used for reflection.
     */
    public static final String INTERCEPTORS_METHOD_NAME = "handleInterceptors";

}
