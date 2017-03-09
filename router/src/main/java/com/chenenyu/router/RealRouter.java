package com.chenenyu.router;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.AnimRes;
import android.support.annotation.Nullable;

import com.chenenyu.router.matcher.Matcher;
import com.chenenyu.router.matcher.MatcherRegistry;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Real router manager, a singleton.
 * <p>
 * Created by Cheney on 2016/12/20.
 */
public class RealRouter {
    // Uri -> Activity
    private Map<String, Class<? extends Activity>> mActivityTable = new HashMap<>();
    // Activity -> mInterceptors' name
    private Map<Class<? extends Activity>, String[]> mInterceptorTable = new HashMap<>();
    // interceptor's name -> interceptor
    private Map<String, Class<? extends RouteInterceptor>> mInterceptors = new HashMap<>();
    // interceptor's name -> interceptor instance
    private Map<String, RouteInterceptor> mInterceptorInstance = new HashMap<>();

    private static RealRouter instance;
    private boolean initialized = false;
    private RouteOptions mRouteOptions = new RouteOptions();
    private Uri uri;

    private RealRouter() {
    }

    static RealRouter get() {
        if (instance == null) {
            synchronized (RealRouter.class) {
                if (instance == null) {
                    instance = new RealRouter();
                }
            }
        }
        instance.reset();
        return instance;
    }

    /**
     * Reset uri and options.
     */
    private void reset() {
        uri = null;
        mRouteOptions = new RouteOptions();
    }

    /**
     * Init.
     */
    synchronized void init() {
        if (initialized) {
            return;
        } else {
            initialized = true;
        }

        /* RouterBuildInfo */
        String[] modules;
        try {
            Class<?> buildInfo = Class.forName(Consts.PACKAGE_NAME + Consts.DOT + Consts.ROUTER_BUILD_INFO);
            Field allModules = buildInfo.getField(Consts.BUILD_INFO_FIELD);
            String modules_name = (String) allModules.get(buildInfo);
            modules = modules_name.split(",");
        } catch (ClassNotFoundException e) {
            RLog.e("Initialization failed, have you forgotten to apply plugin: 'com.chenenyu.router' in application module");
            return;
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        /* RouteTable */
        try {
            String fullTableName;
            for (String moduleName : modules) {
                fullTableName = Consts.PACKAGE_NAME + Consts.DOT + capitalize(moduleName) + Consts.ROUTE_TABLE;
                Class<?> routeTableClz = Class.forName(fullTableName);
                Constructor constructor = routeTableClz.getConstructor();
                RouteTable instance = (RouteTable) constructor.newInstance();
                instance.handleActivityTable(mActivityTable);
            }
        } catch (Exception e) {
            RLog.i(e.getMessage());
        }
        RLog.i("RouteTable", mActivityTable.toString());

        /* InterceptorTable */
        String interceptorTableName;
        for (String moduleName : modules) {
            try {
                interceptorTableName = Consts.PACKAGE_NAME + Consts.DOT + capitalize(moduleName) + Consts.INTERCEPTOR_TABLE;
                Class<?> interceptorTableClz = Class.forName(interceptorTableName);
                Method handleInterceptorTable = interceptorTableClz.getMethod(Consts.HANDLE_INTERCEPTOR_TABLE, Map.class);
                handleInterceptorTable.invoke(null, mInterceptorTable);
            } catch (Exception e) {
                RLog.i(String.format("There is no interceptor table in module: %s.", moduleName));
            }
        }
        RLog.i("InterceptorTable", mInterceptorTable.toString());

        /* Interceptors */
        String interceptorName;
        for (String moduleName : modules) {
            try {
                interceptorName = Consts.PACKAGE_NAME + Consts.DOT + capitalize(moduleName) + Consts.INTERCEPTORS;
                Class<?> interceptorClz = Class.forName(interceptorName);
                Method handleInterceptors = interceptorClz.getMethod(Consts.HANDLE_INTERCEPTORS, Map.class);
                handleInterceptors.invoke(null, mInterceptors);
            } catch (Exception e) {
                RLog.i(String.format("There are no interceptors in module: %s.", moduleName));
            }
        }
        RLog.i("Interceptors", mInterceptors.toString());
    }

    private String capitalize(CharSequence self) {
        return self.length() == 0 ? "" :
                "" + Character.toUpperCase(self.charAt(0)) + self.subSequence(1, self.length());
    }

    /**
     * Add custom route table.
     *
     * @param routeTable RouteTable
     * @see com.chenenyu.router.Router#addRouteTable(RouteTable)
     */
    void addRouteTable(RouteTable routeTable) {
        if (routeTable != null) {
            routeTable.handleActivityTable(mActivityTable);
        }
    }

    RealRouter build(Uri uri) {
        if (!initialized) {
            throw new RuntimeException("Please initialize router first.");
        }
        this.uri = uri;
        return this;
    }

    /**
     * Route result callback.
     *
     * @param callback RouteCallback
     * @return this
     */
    public RealRouter callback(RouteCallback callback) {
        mRouteOptions.setCallback(callback);
        return this;
    }

    /**
     * Request code to start activity for result.
     *
     * @param requestCode requestCode
     * @return this
     */
    public RealRouter requestCode(int requestCode) {
        if (requestCode >= 0) {
            mRouteOptions.setRequestCode(requestCode);
        } else {
            RLog.w("Invalid requestCode");
        }
        return this;
    }

    /**
     * Add extra bundles.
     *
     * @param extras Bundle
     * @return this
     */
    public RealRouter extras(Bundle extras) {
        mRouteOptions.setBundle(extras);
        return this;
    }

    /**
     * Add additional flags to the intent (or with existing flags value).
     *
     * @param flags The new flags to set, such as {@link Intent#FLAG_ACTIVITY_CLEAR_TOP}
     * @return this
     * @see Intent#addFlags(int)
     */
    @SuppressWarnings("unused")
    public RealRouter addFlags(int flags) {
        mRouteOptions.addFlags(flags);
        return this;
    }

    /**
     * Specify an explicit transition animation.
     *
     * @param enterAnim A resource ID of the animation resource to use for the incoming activity.
     *                  Use 0 for no animation.
     * @param exitAnim  A resource ID of the animation resource to use for the outgoing activity.
     *                  Use 0 for no animation.
     * @return this
     * @see Activity#overridePendingTransition(int, int)
     */
    public RealRouter anim(@AnimRes int enterAnim, @AnimRes int exitAnim) {
        mRouteOptions.setAnim(enterAnim, exitAnim);
        return this;
    }

    /**
     * Green channel, i.e. skip all the mInterceptors.
     *
     * @return this
     */
    @SuppressWarnings("unused")
    public RealRouter skipInterceptors() {
        mRouteOptions.setSkipInterceptors(true);
        return this;
    }

    /**
     * {@link RouteCallback}.
     */
    private void callback(RouteResult state, String msg) {
        if (state != RouteResult.SUCCEED) {
            RLog.e(msg);
        }
        if (mRouteOptions.getCallback() != null) {
            mRouteOptions.getCallback().callback(state, uri, msg);
        }
    }

    private void assembleIntent(Context context, Intent intent, RouteOptions routeOptions) {
        if (intent == null) {
            return;
        }
        if (routeOptions.getBundle() != null && !routeOptions.getBundle().isEmpty()) {
            intent.putExtras(routeOptions.getBundle());
        }
        if (!(context instanceof Activity)) {
            routeOptions.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        if (routeOptions.getFlags() != 0) {
            intent.addFlags(routeOptions.getFlags());
        }
    }

    private boolean intercept(Context context, Class<? extends Activity> target) {
        if (mInterceptorTable.isEmpty()) {
            return false;
        }
        String[] interceptors = mInterceptorTable.get(target);
        if (interceptors != null && interceptors.length > 0) {
            for (String name : interceptors) {
                RouteInterceptor interceptor = mInterceptorInstance.get(name);
                if (interceptor == null) {
                    Class<? extends RouteInterceptor> clz = mInterceptors.get(name);
                    try {
                        Constructor<? extends RouteInterceptor> constructor = clz.getConstructor();
                        interceptor = constructor.newInstance();
                        mInterceptorInstance.put(name, interceptor);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (interceptor != null && interceptor.intercept(context, uri, mRouteOptions.getBundle())) {
                    callback(RouteResult.INTERCEPTED, String.format("Intercepted by interceptor: %s.", name));
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Generate an {@link Intent} according to the given uri.
     *
     * @param context Strongly recommend an activity instance.
     * @return Intent
     */
    @Nullable
    @SuppressWarnings("WeakerAccess")
    public Intent getIntent(Context context) {
        if (uri == null) {
            callback(RouteResult.FAILED, "uri == null.");
            return null;
        }

        if (!mRouteOptions.isSkipInterceptors()) {
            for (RouteInterceptor interceptor : Router.getGlobalInterceptors()) {
                if (interceptor.intercept(context, uri, mRouteOptions.getBundle())) {
                    callback(RouteResult.INTERCEPTED, "Intercepted by global interceptor.");
                    return null;
                }
            }
        }

        List<Matcher> matchers = MatcherRegistry.getMatcher();
        if (matchers.isEmpty()) {
            callback(RouteResult.FAILED, "The MatcherRegistry contains no Matcher.");
            return null;
        }

        Set<Map.Entry<String, Class<? extends Activity>>> entries = mActivityTable.entrySet();

        for (Matcher matcher : matchers) {
            if (mActivityTable.isEmpty()) {
                if (matcher.match(context, uri, null, mRouteOptions)) {
                    RLog.i("Caught by " + matcher.getClass().getCanonicalName());
                    Intent intent = matcher.onMatched(context, uri, null);
                    assembleIntent(context, intent, mRouteOptions);
                    return intent;
                }
            } else {
                for (Map.Entry<String, Class<? extends Activity>> entry : entries) {
                    if (matcher.match(context, uri, entry.getKey(), mRouteOptions)) {
                        RLog.i("Caught by " + matcher.getClass().getCanonicalName());
                        if (intercept(context, entry.getValue())) {
                            return null;
                        }
                        Intent intent = matcher.onMatched(context, uri, entry.getValue());
                        assembleIntent(context, intent, mRouteOptions);
                        return intent;
                    }
                }
            }
        }

        callback(RouteResult.FAILED, "Can not find an Activity that matches the given uri: " + uri);
        return null;
    }

    /**
     * Execute transition.
     *
     * @param context Strongly recommend an activity instance.
     */
    public void go(Context context) {
        Intent intent = getIntent(context);
        if (intent == null) {
            return;
        }
        if (mRouteOptions.getRequestCode() >= 0) {
            if (context instanceof Activity) {
                ((Activity) context).startActivityForResult(intent, mRouteOptions.getRequestCode());
            } else {
                RLog.w("Please pass an Activity context to call method 'startActivityForResult'");
                context.startActivity(intent);
            }
        } else {
            context.startActivity(intent);
        }
        if (mRouteOptions.getEnterAnim() != 0 && mRouteOptions.getExitAnim() != 0
                && context instanceof Activity) {
            // Add transition animation.
            ((Activity) context).overridePendingTransition(
                    mRouteOptions.getEnterAnim(), mRouteOptions.getExitAnim());
        }
        callback(RouteResult.SUCCEED, null);
    }

}
