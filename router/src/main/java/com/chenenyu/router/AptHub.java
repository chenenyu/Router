package com.chenenyu.router;

import com.chenenyu.router.util.RLog;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Hub for 'apt' classes.
 * <p>
 * Created by Cheney on 2017/3/13.
 */
class AptHub {
    private static final String PACKAGE_NAME = "com.chenenyu.router";
    private static final String DOT = ".";
    private static final String ROUTER_BUILD_INFO = "RouterBuildInfo";
    private static final String BUILD_INFO_FIELD = "ALL_MODULES";
    private static final String ROUTE_TABLE = "RouteTable";
    private static final String INTERCEPTORS = "Interceptors";
    private static final String TARGET_INTERCEPTORS = "TargetInterceptors";
    static final String PARAM_CLASS_SUFFIX = "$$Router$$ParamInjector";

    // Uri -> Activity/Fragment
    static Map<String, Class<?>> routeTable = new HashMap<>();
    // Activity/Fragment -> interceptorTable' name
    static Map<Class<?>, String[]> targetInterceptors = new HashMap<>();
    // interceptor's name -> interceptor
    static Map<String, Class<? extends RouteInterceptor>> interceptorTable = new HashMap<>();
    // injector's name -> injector
    static Map<String, Class<ParamInjector>> injectors = new HashMap<>();

    synchronized static void init() {
        /* RouterBuildInfo */
        String[] modules;
        try {
            Class<?> buildInfo = Class.forName(PACKAGE_NAME + DOT + ROUTER_BUILD_INFO);
            Field allModules = buildInfo.getField(BUILD_INFO_FIELD);
            String modules_name = (String) allModules.get(buildInfo);
            modules = modules_name.split(",");
        } catch (ClassNotFoundException e) {
            RLog.e("Initialization failed, have you forgotten to apply plugin: " +
                    "'com.chenenyu.router' in application module?");
            return;
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        /* RouteTable */
        String routeTableName;
        for (String moduleName : modules) {
            try {
                routeTableName = PACKAGE_NAME + DOT + capitalize(moduleName) + ROUTE_TABLE;
                Class<?> routeTableClz = Class.forName(routeTableName);
                Constructor constructor = routeTableClz.getConstructor();
                RouteTable instance = (RouteTable) constructor.newInstance();
                instance.handle(routeTable);
            } catch (ClassNotFoundException e) {
                RLog.i(String.format("There is no RouteTable in module: %s.", moduleName));
            } catch (Exception e) {
                RLog.w(e.getMessage());
            }
        }
        RLog.i("RouteTable", routeTable.toString());

        /* TargetInterceptors */
        String targetInterceptorsName;
        for (String moduleName : modules) {
            try {
                targetInterceptorsName = PACKAGE_NAME + DOT + capitalize(moduleName) + TARGET_INTERCEPTORS;
                Class<?> clz = Class.forName(targetInterceptorsName);
                Constructor constructor = clz.getConstructor();
                TargetInterceptors instance = (TargetInterceptors) constructor.newInstance();
                instance.handle(targetInterceptors);
            } catch (ClassNotFoundException e) {
                RLog.i(String.format("There is no TargetInterceptors in module: %s.", moduleName));
            } catch (Exception e) {
                RLog.w(e.getMessage());
            }
        }
        if (!targetInterceptors.isEmpty()) {
            RLog.i("TargetInterceptors", targetInterceptors.toString());
        }

        /* InterceptorTable */
        String interceptorName;
        for (String moduleName : modules) {
            try {
                interceptorName = PACKAGE_NAME + DOT + capitalize(moduleName) + INTERCEPTORS;
                Class<?> clz = Class.forName(interceptorName);
                Constructor constructor = clz.getConstructor();
                InterceptorTable instance = (InterceptorTable) constructor.newInstance();
                instance.handle(interceptorTable);
            } catch (ClassNotFoundException e) {
                RLog.i(String.format("There is no InterceptorTable in module: %s.", moduleName));
            } catch (Exception e) {
                RLog.w(e.getMessage());
            }
        }
        if (!interceptorTable.isEmpty()) {
            RLog.i("InterceptorTable", interceptorTable.toString());
        }
    }

    private static String capitalize(CharSequence self) {
        return self.length() == 0 ? "" :
                "" + Character.toUpperCase(self.charAt(0)) + self.subSequence(1, self.length());
    }
}
