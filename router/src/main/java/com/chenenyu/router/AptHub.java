package com.chenenyu.router;

import com.chenenyu.router.util.RLog;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
    private static final String HANDLE_INTERCEPTORS = "handle";
    private static final String INTERCEPTOR_TABLE = "InterceptorTable";
    private static final String HANDLE_INTERCEPTOR_TABLE = "handle";

    // Uri -> Activity/Fragment
    static Map<String, Class<?>> routeTable = new HashMap<>();
    // Activity -> interceptors' name
    static Map<Class<?>, String[]> interceptorTable = new HashMap<>();
    // interceptor's name -> interceptor
    static Map<String, Class<? extends RouteInterceptor>> interceptors = new HashMap<>();

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
                    "'com.chenenyu.router' in application module");
            return;
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        /* RouteTable */
        String fullTableName;
        for (String moduleName : modules) {
            try {
                fullTableName = PACKAGE_NAME + DOT + capitalize(moduleName) + ROUTE_TABLE;
                Class<?> routeTableClz = Class.forName(fullTableName);
                Constructor constructor = routeTableClz.getConstructor();
                RouteTable instance = (RouteTable) constructor.newInstance();
                instance.handle(routeTable);
            } catch (ClassNotFoundException e) {
                RLog.i(String.format("There is no route table in module: %s.", moduleName));
            } catch (Exception e) {
                RLog.w(e.getMessage());
            }
        }
        RLog.i("RouteTable", routeTable.toString());

        /* InterceptorTable */
        String interceptorTableName;
        for (String moduleName : modules) {
            try {
                interceptorTableName = PACKAGE_NAME + DOT + capitalize(moduleName) + INTERCEPTOR_TABLE;
                Class<?> clz = Class.forName(interceptorTableName);
                Method handle = clz.getMethod(HANDLE_INTERCEPTOR_TABLE, Map.class);
                handle.invoke(null, interceptorTable);
            } catch (ClassNotFoundException e) {
                RLog.i(String.format("There is no interceptor table in module: %s.", moduleName));
            } catch (Exception e) {
                RLog.w(e.getMessage());
            }
        }
        if (!interceptorTable.isEmpty()) {
            RLog.i("InterceptorTable", interceptorTable.toString());
        }

        /* Interceptors */
        String interceptorName;
        for (String moduleName : modules) {
            try {
                interceptorName = PACKAGE_NAME + DOT + capitalize(moduleName) + INTERCEPTORS;
                Class<?> interceptorClz = Class.forName(interceptorName);
                Method handle = interceptorClz.getMethod(HANDLE_INTERCEPTORS, Map.class);
                handle.invoke(null, interceptors);
            } catch (ClassNotFoundException e) {
                RLog.i(String.format("There are no interceptors in module: %s.", moduleName));
            } catch (Exception e) {
                RLog.w(e.getMessage());
            }
        }
        if (!interceptors.isEmpty()) {
            RLog.i("Interceptors", interceptors.toString());
        }
    }

    private static String capitalize(CharSequence self) {
        return self.length() == 0 ? "" :
                "" + Character.toUpperCase(self.charAt(0)) + self.subSequence(1, self.length());
    }
}
