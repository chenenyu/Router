package com.chenenyu.router;

import android.app.Activity;

import androidx.fragment.app.Fragment;

import com.chenenyu.router.template.ParamInjector;
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
    private static final String PARAM_CLASS_SUFFIX = "$$Router$$ParamInjector";

    // injector's name -> injector
    private static final Map<String, Class<ParamInjector>> injectors = new HashMap<>();

    // Uri -> Activity/Fragment
    public final static Map<String, Class<?>> routeTable = new HashMap<>();

    // interceptor's name -> interceptor
    public final static Map<String, Class<? extends RouteInterceptor>> interceptorTable = new HashMap<>();
    // interceptor instance
    public final static Map<String, RouteInterceptor> interceptorInstances = new HashMap<>();

    // Activity/Fragment -> interceptors' name
    // Note: 这里用LinkedHashMap保证有序
    public final static Map<Class<?>, String[]> targetInterceptorsTable = new LinkedHashMap<>();


    /**
     * Auto inject params from bundle.
     *
     * @param obj Activity or Fragment.
     */
    @SuppressWarnings("unchecked")
    static void injectParams(Object obj) {
        if (obj instanceof Activity || obj instanceof Fragment) {
            String key = obj.getClass().getCanonicalName();
            Class<ParamInjector> clz;
            if (!injectors.containsKey(key)) {
                try {
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
