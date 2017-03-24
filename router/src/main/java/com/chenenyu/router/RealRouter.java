package com.chenenyu.router;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.AnimRes;
import android.support.annotation.Nullable;

import com.chenenyu.router.matcher.AbsImplicitMatcher;
import com.chenenyu.router.matcher.Matcher;
import com.chenenyu.router.matcher.MatcherRegistry;

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
    private static RealRouter sInstance;

    // interceptor's name -> interceptor instance
    private Map<String, RouteInterceptor> mInterceptorInstance = new HashMap<>();
    private RouteOptions mRouteOptions;
    private Uri uri;

    private RealRouter() {
    }

    static RealRouter get() {
        if (sInstance == null) {
            synchronized (RealRouter.class) {
                if (sInstance == null) {
                    sInstance = new RealRouter();
                }
            }
        }
        sInstance.reset();
        return sInstance;
    }

    /**
     * Reset uri and options.
     */
    private void reset() {
        uri = null;
        mRouteOptions = new RouteOptions();
    }

    /**
     * Add custom route table.
     *
     * @param routeTable RouteTable
     * @see com.chenenyu.router.Router#addRouteTable(RouteTable)
     */
    void addRouteTable(RouteTable routeTable) {
        if (routeTable != null) {
            routeTable.handleActivityTable(AptHub.activityTable);
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
     * Green channel, i.e. skip all the interceptors.
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
            RLog.w(msg);
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
        if (AptHub.interceptorTable.isEmpty()) {
            return false;
        }
        String[] interceptors = AptHub.interceptorTable.get(target);
        if (interceptors != null && interceptors.length > 0) {
            for (String name : interceptors) {
                RouteInterceptor interceptor = mInterceptorInstance.get(name);
                if (interceptor == null) {
                    Class<? extends RouteInterceptor> clz = AptHub.interceptors.get(name);
                    try {
                        Constructor<? extends RouteInterceptor> constructor = clz.getConstructor();
                        interceptor = constructor.newInstance();
                        mInterceptorInstance.put(name, interceptor);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (interceptor != null && interceptor.intercept(context, uri,
                        mRouteOptions.getBundle())) {
                    callback(RouteResult.INTERCEPTED,
                            String.format("Intercepted by interceptor: %s.", name));
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Generate an {@link Intent} according to the given uri.
     *
     * @param context Strongly recommend an activity sInstance.
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

        Set<Map.Entry<String, Class<? extends Activity>>> entries = AptHub.activityTable.entrySet();

        for (Matcher matcher : matchers) {
            if (AptHub.activityTable.isEmpty()) {
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
                        // Ignore implicit intent.
                        if (!(matcher instanceof AbsImplicitMatcher) &&
                                intercept(context, entry.getValue())) {
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
     * @param context Strongly recommend an activity sInstance.
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
