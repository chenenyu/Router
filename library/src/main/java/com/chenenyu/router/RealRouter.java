package com.chenenyu.router;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
    private static RealRouter instance = new RealRouter();
    private Map<String, Class<? extends Activity>> mapping = new HashMap<>();

    private Matcher defaultMatcher = new DefaultMatcher();

    private Uri uri;
    private int flags;
    private int requestCode = -1;
    @Nullable
    private RouteCallBack callback;
    @Nullable
    private Bundle extras;
    private int enterAnim;
    private int exitAnim;

    private RealRouter() {
        initMapping();
    }

    static RealRouter get() {
        instance.reset();
        return instance;
    }

    /**
     * Reset fields.
     */
    private void reset() {
        uri = null;
        flags = 0;
        requestCode = -1;
        callback = null;
        extras = null;
        enterAnim = 0;
        exitAnim = 0;
    }

    /**
     * Init annotated route table.
     */
    private void initMapping() {
        try {
            Class<?> annotatedRouteTable = Class.forName("com.chenenyu.router.AnnotatedRouteTable");
            Constructor constructor = annotatedRouteTable.getConstructor();
            RouteTable instance = (RouteTable) constructor.newInstance();
            instance.initActivityTable(mapping);
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
            routeTable.initActivityTable(mapping);
        }
    }

    RealRouter build(Uri uri) {
        this.uri = uri;
        return this;
    }

    /**
     * Route result callback.
     *
     * @param callback RouteCallBack
     * @return this
     */
    public RealRouter callback(RouteCallBack callback) {
        this.callback = callback;
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
            this.requestCode = requestCode;
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
        this.extras = extras;
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
        this.flags |= flags;
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
        this.enterAnim = enterAnim;
        this.exitAnim = exitAnim;
        return this;
    }

    /**
     * {@link RouteCallBack} succeed.
     *
     * @param uri Uri
     */
    private void succeed(Uri uri) {
        if (callback != null) {
            callback.succeed(uri);
        }
    }

    /**
     * {@link RouteCallBack} error.
     *
     * @param uri     Uri
     * @param message Error message
     */
    private void error(Uri uri, String message) {
        if (callback != null) {
            callback.error(uri, message);
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
        if (mapping.isEmpty()) {
            error(null, "the route table is empty.");
            return null;
        }

        for (RouteInterceptor interceptor : Router.getRouteInterceptors()) {
            if (interceptor.intercept(context, uri, extras)) {
                error(uri, "intercepted.");
                return null;
            }
        }

        for (Map.Entry<String, Class<? extends Activity>> entry : mapping.entrySet()) {
            List<Matcher> customMatchers = Router.getMatchers();
            for (Matcher matcher : customMatchers) {
                if (matcher.match(uri, entry.getKey())) {
                    return generateIntent(context, entry.getValue());
                }
            }
            if (defaultMatcher.match(uri, entry.getKey())) {
                return generateIntent(context, entry.getValue());
            }
        }
        if (uri.toString().toLowerCase().startsWith("http://")
                || uri.toString().toLowerCase().startsWith("https://")) {
            RLog.i("It seems that you are trying to open a http(s) url.");
            return new Intent(Intent.ACTION_VIEW, uri);
        }

        error(uri, "Can not find an Activity that matches the given uri.");
        return null;
    }

    private Intent generateIntent(Context context, Class<? extends Activity> clz) {
        Intent intent = new Intent(context, clz);
        if (extras != null) {
            intent.putExtras(extras);
        }
        if (!(context instanceof Activity)) {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        if (this.flags != 0) {
            intent.addFlags(this.flags);
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
        if (requestCode >= 0) {
            if (context instanceof Activity) {
                ((Activity) context).startActivityForResult(intent, requestCode);
            } else {
                RLog.w("Please pass an Activity context to call method 'startActivityForResult'");
                context.startActivity(intent);
            }
        } else {
            context.startActivity(intent);
        }
        if (enterAnim != 0 && exitAnim != 0 && context instanceof Activity) {
            // Add transition animation.
            ((Activity) context).overridePendingTransition(enterAnim, exitAnim);
        }
        succeed(uri);
    }

}
