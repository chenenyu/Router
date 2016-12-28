package com.chenenyu.router;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
    @Nullable
    private RouteCallBack callback;
    private int requestCode = -1;
    @Nullable
    private Bundle extras;

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
        callback = null;
        uri = null;
        requestCode = -1;
        extras = null;
    }

    /**
     * Init annotated route table.
     */
    void initMapping() {
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
     * @param message error message
     */
    private void error(Uri uri, String message) {
        if (callback != null) {
            callback.error(uri, message);
        }
    }

    /**
     * Generate an {@link Intent} according to the given uri.
     *
     * @param context Context
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
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        if (extras != null) {
            intent.putExtras(extras);
        }
        return intent;
    }

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
        succeed(uri);
    }

}
