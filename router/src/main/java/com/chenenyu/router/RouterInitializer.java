package com.chenenyu.router;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.startup.Initializer;

import com.chenenyu.router.template.InterceptorTable;
import com.chenenyu.router.template.RouteTable;
import com.chenenyu.router.template.TargetInterceptorsTable;
import com.chenenyu.router.util.RLog;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author chenenyu
 * @date 2021/10/26
 */
public class RouterInitializer implements Initializer<Void> {
    /**
     * meta-data const value in manifest.
     */
    private static final String META_VALUE = "com.chenenyu.router.moduleName";

    private static final String PACKAGE_PREFIX = "com.chenenyu.router.apt";

    @NonNull
    @Override
    public Void create(@NonNull Context context) {
        init(context);
        return null;
    }

    @NonNull
    @Override
    public List<Class<? extends Initializer<?>>> dependencies() {
        return Collections.emptyList();
    }

    private void init(Context context) {
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle metadata = applicationInfo.metaData;
            if (metadata != null) {
                Set<String> keys = metadata.keySet();
                for (String key : keys) {
                    String value = metadata.getString(key, null);
                    if (META_VALUE.equals(value)) {
                        String validModuleName = key.replace(".", "_").replace("-", "_");
                        injectRouteTable(validModuleName);
                        injectInterceptorTable(validModuleName);
                        injectTargetInterceptorsTable(validModuleName);
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    private String capitalize(CharSequence self) {
        return self.length() == 0 ? "" :
                "" + Character.toUpperCase(self.charAt(0)) + self.subSequence(1, self.length());
    }

    private void injectRouteTable(String moduleName) {
        try {
            Class<?> clazz = Class.forName(PACKAGE_PREFIX + "." + capitalize(moduleName) + "RouteTable");
            if (RouteTable.class.isAssignableFrom(clazz)) {
                Class<? extends RouteTable> component = (Class<? extends RouteTable>) clazz;
                RouteTable instance = component.newInstance();
                instance.handle(AptHub.routeTable);
            } else {
                RLog.w(clazz.getCanonicalName() + " does not implements RouteTable.");
            }
        } catch (ClassNotFoundException e) {
            // pass
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    private void injectInterceptorTable(String moduleName) {
        try {
            Class<?> clazz = Class.forName(PACKAGE_PREFIX + "." + capitalize(moduleName) + "InterceptorTable");
            if (InterceptorTable.class.isAssignableFrom(clazz)) {
                Class<? extends InterceptorTable> component = (Class<? extends InterceptorTable>) clazz;
                InterceptorTable instance = component.newInstance();
                instance.handle(AptHub.interceptorTable);
            } else {
                RLog.w(clazz.getCanonicalName() + " does not implements InterceptorTable.");
            }
        } catch (ClassNotFoundException e) {
            // pass
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    private void injectTargetInterceptorsTable(String moduleName) {
        try {
            Class<?> clazz = Class.forName(PACKAGE_PREFIX + "." + capitalize(moduleName) + "TargetInterceptorsTable");
            if (TargetInterceptorsTable.class.isAssignableFrom(clazz)) {
                Class<? extends TargetInterceptorsTable> component = (Class<? extends TargetInterceptorsTable>) clazz;
                TargetInterceptorsTable instance = component.newInstance();
                instance.handle(AptHub.targetInterceptorsTable);
            } else {
                RLog.w(clazz.getCanonicalName() + " does not implements TargetInterceptorsTable.");
            }
        } catch (ClassNotFoundException e) {
            // pass
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }
}
