package com.chenenyu.router;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.AnimRes;
import android.support.annotation.Nullable;

import com.chenenyu.router.matcher.BrowserMatcher;
import com.chenenyu.router.matcher.Matcher;
import com.chenenyu.router.matcher.MatcherRepository;
import com.chenenyu.router.matcher.SchemeMatcher;

import java.lang.reflect.Constructor;
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
    private static RealRouter instance = new RealRouter();
    private Map<String, Class<? extends Activity>> mapping = new HashMap<>();
    private RouteOptions routeOptions = new RouteOptions();
    private Uri uri;


    private RealRouter() {
        initMapping();
    }

    static RealRouter get() {
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

        for (RouteInterceptor interceptor : Router.getRouteInterceptors()) {
            if (interceptor.intercept(context, uri, routeOptions.getBundle())) {
                error(uri, "intercepted.");
                return null;
            }
        }

        List<Matcher> matcherList = MatcherRepository.getMatcher();
        if (matcherList.isEmpty()) {
            error(uri, "The MatcherRepository contains no Matcher.");
            return null;
        }
        Set<Map.Entry<String, Class<? extends Activity>>> entries = mapping.entrySet();

        for (Matcher matcher : matcherList) {
            if (mapping.isEmpty()) {
                if (matcher.match(context, uri, null, routeOptions)) {
                    RLog.i("Caught by " + matcher.getClass().getCanonicalName());
                    if (matcher instanceof SchemeMatcher) {
                        // Implicit intent
                        RLog.i("Trying to open an Activity by implicit intent.");
                        return generateIntent(context, new Intent().setData(uri));
                    } else if (matcher instanceof BrowserMatcher) {
                        return new Intent(Intent.ACTION_VIEW, uri);
                    } else {
                        return null;
                    }
                }
            } else {
                for (Map.Entry<String, Class<? extends Activity>> entry : entries) {
                    if (matcher.match(context, uri, entry.getKey(), routeOptions)) {
                        RLog.i("Caught by " + matcher.getClass().getCanonicalName());
                        if (matcher instanceof SchemeMatcher) {
                            // Implicit intent
                            RLog.i("Trying to open an Activity by implicit intent.");
                            return generateIntent(context, new Intent().setData(uri));
                        } else if (matcher instanceof BrowserMatcher) {
                            return new Intent(Intent.ACTION_VIEW, uri);
                        } else {
                            // Explicit intent
                            return generateIntent(context, entry.getValue());
                        }
                    }
                }
            }
        }

        error(uri, "Could not find an Activity that matches the given uri.");
        return null;
    }

    private Intent generateIntent(Context context, Class<? extends Activity> clz) {
        return generateIntent(context, new Intent(context, clz));
    }

    private Intent generateIntent(Context context, Intent intent) {
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
