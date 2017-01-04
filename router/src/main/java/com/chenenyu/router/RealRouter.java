package com.chenenyu.router;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.AnimRes;
import android.support.annotation.Nullable;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Real router manager, a singleton.
 * <p>
 * Created by Cheney on 2016/12/20.
 */
public class RealRouter {
    private static RealRouter _instance = new RealRouter();
    private Map<String, Class<? extends Activity>> mapping = new HashMap<>();
    private Matcher defaultMatcher = new DefaultMatcher();
    private RouteOptions routeOptions = new RouteOptions();
    private Uri uri;

    private RealRouter() {
        initMapping();
    }

    static RealRouter get() {
        _instance.reset();
        return _instance;
    }

    /**
     * Reset uri and options.
     */
    private void reset() {
        uri = null;
        routeOptions.reset();
    }

    /**
     * Init annotated route table.
     */
    private void initMapping() {
        try {
            Class<?> annotatedRouteTable = Class.forName("com.chenenyu.router.AnnotatedRouteTable");
            Constructor constructor = annotatedRouteTable.getConstructor();
            RouteTable instance = (RouteTable) constructor.newInstance();
            instance.handleActivityTable(mapping);
        } catch (Exception e) {
            RLog.i("Failed to find/generate class 'com.chenenyu.router.AnnotatedRouteTable'.", e);
        }
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
        if (routeOptions.getCallback() != null) {
            routeOptions.getCallback().error(uri, message);
        }
    }

    /**
     * Generate an {@link Intent} according to the given uri.
     *
     * @param context Strongly recommend an activity _instance.
     * @return Intent
     */
    @Nullable
    private Intent getIntent(Context context) {
        if (uri == null) {
            error(null, "uri == null.");
            return null;
        }
        if (mapping.isEmpty()) {
            error(null, "the route table is empty.");
            return null;
        }

        for (RouteInterceptor interceptor : Router.getRouteInterceptors()) {
            if (interceptor.intercept(context, uri, routeOptions.getBundle())) {
                error(uri, "intercepted.");
                return null;
            }
        }

        for (Map.Entry<String, Class<? extends Activity>> entry : mapping.entrySet()) {
            List<Matcher> customMatcher = Router.getMatcher();
            for (Matcher matcher : customMatcher) {
                if (matcher.match(uri, entry.getKey(), routeOptions)) {
                    return generateIntent(context, entry.getValue());
                }
            }
            if (defaultMatcher.match(uri, entry.getKey(), routeOptions)) {
                return generateIntent(context, entry.getValue());
            }
        }
        if (uri.toString().toLowerCase().startsWith("http://")
                || uri.toString().toLowerCase().startsWith("https://")) {
            RLog.i("It seems that you are trying to open a http(s) url.");
            return new Intent(Intent.ACTION_VIEW, uri);
        }

        Intent intent = new Intent();
        intent.setData(uri);
        if (context.getPackageManager().resolveActivity(intent,
                PackageManager.MATCH_DEFAULT_ONLY) != null) {
            Uri uri = intent.getData();
            String queryString = uri.getQuery();
            Map<String, String> params = new HashMap<>();
            Utils.parseParams(params, queryString);
            Bundle bundle = new Bundle();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                bundle.putString(entry.getKey(), entry.getValue());
            }
            intent.putExtras(bundle);
            return intent;
        }

        error(uri, "Could not find an Activity that matches the given uri.");
        return null;
    }

    private Intent generateIntent(Context context, Class<? extends Activity> clz) {
        Intent intent = new Intent(context, clz);
        if (routeOptions.getBundle() != null && !routeOptions.getBundle().isEmpty()) {
            intent.putExtras(routeOptions.getBundle());
        }
        if (!(context instanceof Activity)) {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        if (routeOptions.getFlags() != 0) {
            intent.addFlags(routeOptions.getFlags());
        }
        return intent;
    }

    /**
     * Execute transition.
     *
     * @param context Strongly recommend an activity _instance.
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
