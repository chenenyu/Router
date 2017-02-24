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
    private static RealRouter instance;
    private Map<String, Class<? extends Activity>> mapping = new HashMap<>();
    private boolean initialized = false;
    private RouteOptions routeOptions = new RouteOptions();
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
        routeOptions.reset();
    }

    /**
     * Init route table.
     */
    synchronized void initMapping() {
        if (initialized) {
            RLog.e("Initialized mapping.");
            return;
        } else {
            initialized = true;
        }

        String[] modules;
        try {
            Class<?> configClz = Class.forName("com.chenenyu.router.RouterBuildConfig");
            Field allModules = configClz.getField("ALL_MODULES");
            String modules_name = (String) allModules.get(configClz);
            modules = modules_name.split(",");
        } catch (ClassNotFoundException e) {
            RLog.e("Have you applied plugin 'com.chenenyu.router' in your application module?", e);
            return;
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        try {
            String fullTableName;
            for (String moduleName : modules) {
                fullTableName = "com.chenenyu.router." + capitalize(moduleName) + "RouteTable";
                Class<?> moduleRouteTable = Class.forName(fullTableName);
                Constructor constructor = moduleRouteTable.getConstructor();
                RouteTable instance = (RouteTable) constructor.newInstance();
                instance.handleActivityTable(mapping);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        RLog.i("RouteTable", mapping.toString());
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
            routeTable.handleActivityTable(mapping);
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
        routeOptions.setCallback(callback);
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
            routeOptions.setRequestCode(requestCode);
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
        routeOptions.setBundle(extras);
        return this;
    }

    /**
     * Add additional flags to the intent (or with existing flags value).
     *
     * @param flags The new flags to set, such as {@link Intent#FLAG_ACTIVITY_CLEAR_TOP}
     * @return this
     * @see Intent#addFlags(int)
     */
    public RealRouter addFlags(int flags) {
        routeOptions.addFlags(flags);
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
        routeOptions.setAnim(enterAnim, exitAnim);
        return this;
    }

    /**
     * Green channel, i.e. skip all the interceptors.
     *
     * @return this
     */
    public RealRouter skipInterceptors() {
        routeOptions.setSkipInterceptors(true);
        return this;
    }

    /**
     * {@link RouteCallback} succeed.
     *
     * @param uri Uri
     */
    private void succeed(Uri uri) {
        if (routeOptions.getCallback() != null) {
            routeOptions.getCallback().succeed(uri);
        }
    }

    /**
     * {@link RouteCallback} error.
     *
     * @param uri     Uri
     * @param message Error message
     */
    private void error(Uri uri, String message) {
        RLog.e(message);
        if (routeOptions.getCallback() != null) {
            routeOptions.getCallback().error(uri, message);
        }
    }

    /**
     * Generate an {@link Intent} according to the given uri.
     *
     * @param context Strongly recommend an activity instance.
     * @return Intent
     */
    @Nullable
    public Intent getIntent(Context context) {
        if (uri == null) {
            error(null, "uri == null.");
            return null;
        }

        if (!routeOptions.isSkipInterceptors()) {
            for (RouteInterceptor interceptor : Router.getRouteInterceptors()) {
                if (interceptor.intercept(context, uri, routeOptions.getBundle())) {
                    error(uri, "intercepted.");
                    return null;
                }
            }
        }

        List<Matcher> matcherList = MatcherRegistry.getMatcher();
        if (matcherList.isEmpty()) {
            error(uri, "The MatcherRegistry contains no Matcher.");
            return null;
        }

        Set<Map.Entry<String, Class<? extends Activity>>> entries = mapping.entrySet();

        for (Matcher matcher : matcherList) {
            if (mapping.isEmpty()) {
                if (matcher.match(context, uri, null, routeOptions)) {
                    RLog.i("Caught by " + matcher.getClass().getCanonicalName());
                    Intent intent = matcher.onMatched(context, uri, null);
                    assembleIntent(context, intent, routeOptions);
                    return intent;
                }
            } else {
                for (Map.Entry<String, Class<? extends Activity>> entry : entries) {
                    if (matcher.match(context, uri, entry.getKey(), routeOptions)) {
                        RLog.i("Caught by " + matcher.getClass().getCanonicalName());
                        Intent intent = matcher.onMatched(context, uri, entry.getValue());
                        assembleIntent(context, intent, routeOptions);
                        return intent;
                    }
                }
            }
        }

        error(uri, "Could not find an Activity that matches the given uri.");
        return null;
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
        if (routeOptions.getRequestCode() >= 0) {
            if (context instanceof Activity) {
                ((Activity) context).startActivityForResult(intent, routeOptions.getRequestCode());
            } else {
                RLog.w("Please pass an Activity context to call method 'startActivityForResult'");
                context.startActivity(intent);
            }
        } else {
            context.startActivity(intent);
        }
        if (routeOptions.getEnterAnim() != 0 && routeOptions.getExitAnim() != 0
                && context instanceof Activity) {
            // Add transition animation.
            ((Activity) context).overridePendingTransition(
                    routeOptions.getEnterAnim(), routeOptions.getExitAnim());
        }
        succeed(uri);
    }

}
