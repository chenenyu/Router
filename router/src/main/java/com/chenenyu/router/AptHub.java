package com.chenenyu.router;

import android.app.Activity;
import android.app.Fragment;

import com.chenenyu.router.template.InterceptorTable;
import com.chenenyu.router.template.ParamInjector;
import com.chenenyu.router.template.RouteTable;
import com.chenenyu.router.template.TargetInterceptorsTable;
import com.chenenyu.router.util.RLog;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Hub for 'apt' classes.
 * <p>
 * Created by chenenyu on 2017/3/13.
 */
public final class AptHub {
    private static final String PACKAGE_NAME = "com.chenenyu.router";
    private static final String DOT = ".";
    private static final String ROUTE_TABLE = "RouteTable";
    private static final String INTERCEPTOR_TABLE = "InterceptorTable";
    private static final String TARGET_INTERCEPTORS_TABLE = "TargetInterceptorsTable";
    private static final String PARAM_CLASS_SUFFIX = "$$Router$$ParamInjector";

    // Uri -> Activity/Fragment
    public final static Map<String, Class<?>> routeTable = new HashMap<>();

    // interceptor's name -> interceptor
    public final static Map<String, Class<? extends RouteInterceptor>> interceptorTable = new HashMap<>();
    // interceptor instance
    public final static Map<String, RouteInterceptor> interceptorInstances = new HashMap<>();

    // Activity/Fragment -> interceptors' name
    // Note: 这里用LinkedHashMap保证有序
    public final static Map<Class<?>, String[]> targetInterceptorsTable = new LinkedHashMap<>();

    // injector's name -> injector
    private static Map<String, Class<ParamInjector>> injectors = new HashMap<>();

    /**
     * This method offers an ability to register modules for developers.
     *
     * @param modules extra modules' name
     */
    synchronized static void registerModules(String... modules) {
        if (modules == null || modules.length == 0) {
            RLog.w("empty modules.");
        } else {
            // format module name first.
            formatModuleName(modules);

            /* RouteTable */
            String routeTableName;
            for (String module : modules) {
                try {
                    routeTableName = PACKAGE_NAME + DOT + capitalize(module) + ROUTE_TABLE;
                    Class<?> clz = Class.forName(routeTableName);
                    RouteTable instance = (RouteTable) clz.newInstance();
                    instance.handle(routeTable);
                } catch (ClassNotFoundException e) {
                    RLog.i("RouteTable", String.format("There is no RouteTable in module: %s.", module));
                } catch (Exception e) {
                    RLog.w(e.getMessage());
                }
            }
            RLog.i("RouteTable", routeTable.toString());

            /* TargetInterceptorsTable */
            String targetInterceptorsName;
            for (String moduleName : modules) {
                try {
                    targetInterceptorsName = PACKAGE_NAME + DOT + capitalize(moduleName) + TARGET_INTERCEPTORS_TABLE;
                    Class<?> clz = Class.forName(targetInterceptorsName);
                    TargetInterceptorsTable instance = (TargetInterceptorsTable) clz.newInstance();
                    instance.handle(targetInterceptorsTable);
                } catch (ClassNotFoundException e) {
                    // RLog.i(String.format("There is no TargetInterceptorTable in module: %s.", moduleName));
                } catch (Exception e) {
                    RLog.w(e.getMessage());
                }
            }

            /* InterceptorTable */
            String interceptorName;
            for (String moduleName : modules) {
                try {
                    interceptorName = PACKAGE_NAME + DOT + capitalize(moduleName) + INTERCEPTOR_TABLE;
                    Class<?> clz = Class.forName(interceptorName);
                    InterceptorTable instance = (InterceptorTable) clz.newInstance();
                    instance.handle(interceptorTable);
                } catch (ClassNotFoundException e) {
                    // RLog.i("InterceptorTable", String.format("There is no InterceptorTable in module: %s.", moduleName));
                } catch (Exception e) {
                    RLog.w(e.getMessage());
                }
            }
            if (!interceptorTable.isEmpty()) {
                RLog.i("InterceptorTable", interceptorTable.toString());
            }
        }
    }

    private static String capitalize(CharSequence self) {
        return self.length() == 0 ? "" :
                "" + Character.toUpperCase(self.charAt(0)) + self.subSequence(1, self.length());
    }

    private static void formatModuleName(String... modules) {
        for (int i = 0; i < modules.length; i++) {
            modules[i] = modules[i].replace('.', '_').replace('-', '_');
        }
    }

    /**
     * Auto inject params from bundle.
     *
     * @param obj Activity or Fragment.
     */
    static void injectParams(Object obj) {
        if (obj instanceof Activity || obj instanceof Fragment || obj instanceof android.support.v4.app.Fragment) {
            String key = obj.getClass().getCanonicalName();
            Class<ParamInjector> clz;
            if (!injectors.containsKey(key)) {
                try {
                    //noinspection unchecked
                    clz = (Class<ParamInjector>) Class.forName(key + PARAM_CLASS_SUFFIX);
                    injectors.put(key, clz);
                } catch (ClassNotFoundException e) {
                    RLog.e("Inject params failed.", e);
                    return;
                }
            } else {
                clz = injectors.get(key);
            }
            try {
                ParamInjector injector = clz.newInstance();
                injector.inject(obj);
            } catch (Exception e) {
                RLog.e("Inject params failed.", e);
            }
        } else {
            RLog.e("The obj you passed must be an instance of Activity or Fragment.");
        }
    }
}
