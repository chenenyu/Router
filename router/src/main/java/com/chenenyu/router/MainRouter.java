package com.chenenyu.router;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;

import com.chenenyu.router.matcher.AbsImplicitMatcher;
import com.chenenyu.router.matcher.AbsMatcher;
import com.chenenyu.router.matcher.MatcherRegistry;
import com.chenenyu.router.util.RLog;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * Created by Cheney on 2017/3/30.
 */
class MainRouter extends AbsRouter {
    private static MainRouter sInstance;
    private Map<String, RouteInterceptor> mInterceptorInstance = new HashMap<>();

    private MainRouter() {
    }

    static synchronized MainRouter getInstance() {
        if (sInstance == null) {
            sInstance = new MainRouter();
        }
        return sInstance;
    }

    /**
     * Add custom route table.
     *
     * @param routeTable RouteTable
     * @see com.chenenyu.router.Router#addRouteTable(RouteTable)
     */
    void addRouteTable(RouteTable routeTable) {
        if (routeTable != null) {
            routeTable.handle(AptHub.routeTable);
        }
    }

    private void callback(RouteResult result, String msg) {
        if (result != RouteResult.SUCCEED) {
            RLog.w(msg);
        }
        if (mRouteRequest.getCallback() != null) {
            mRouteRequest.getCallback().callback(result, mRouteRequest.getUri(), msg);
        }
    }

    @Override
    public Object getFragment(Context context) {
        if (mRouteRequest.getUri() == null) {
            callback(RouteResult.FAILED, "uri == null.");
            return null;
        }

        if (!mRouteRequest.isSkipInterceptors()) {
            for (RouteInterceptor interceptor : Router.getGlobalInterceptors()) {
                if (interceptor.intercept(context, mRouteRequest.getUri(), mRouteRequest.getExtras())) {
                    callback(RouteResult.INTERCEPTED, "Intercepted by global interceptor.");
                    return null;
                }
            }
        }

        List<AbsMatcher> matchers = MatcherRegistry.getMatcher();
        if (matchers.isEmpty()) {
            callback(RouteResult.FAILED, "The MatcherRegistry contains no Matcher.");
            return null;
        }

        if (AptHub.routeTable.isEmpty()) {
            callback(RouteResult.FAILED, "The route table contains no mapping.");
            return null;
        }

        Set<Map.Entry<String, Class<?>>> entries = AptHub.routeTable.entrySet();

        for (AbsMatcher matcher : matchers) {
            if (matcher instanceof AbsImplicitMatcher) { // Ignore implicit matcher.
                continue;
            }
            for (Map.Entry<String, Class<?>> entry : entries) {
                if (matcher.match(context, mRouteRequest.getUri(), entry.getKey(), mRouteRequest)) {
                    RLog.i("Caught by " + matcher.getClass().getCanonicalName());
                    if (intercept(context, entry.getValue())) {
                        return null;
                    }
                    Object result = matcher.generate(context, mRouteRequest.getUri(), entry.getValue());
                    if (result instanceof Fragment) {
                        Fragment fragment = (Fragment) result;
                        Bundle bundle = mRouteRequest.getExtras();
                        if (bundle != null && !bundle.isEmpty()) {
                            fragment.setArguments(bundle);
                        }
                        return fragment;
                    }
                    if (result instanceof android.app.Fragment) {
                        android.app.Fragment fragment = (android.app.Fragment) result;
                        Bundle bundle = mRouteRequest.getExtras();
                        if (bundle != null && !bundle.isEmpty()) {
                            fragment.setArguments(bundle);
                        }
                        return fragment;
                    }
                    return null;
                }
            }
        }

        callback(RouteResult.FAILED, "Can not find an Fragment that matches the given uri: "
                + mRouteRequest.getUri());
        return null;
    }

    @Override
    public Intent getIntent(Context context) {
        if (mRouteRequest.getUri() == null) {
            callback(RouteResult.FAILED, "uri == null.");
            return null;
        }

        if (!mRouteRequest.isSkipInterceptors()) {
            for (RouteInterceptor interceptor : Router.getGlobalInterceptors()) {
                if (interceptor.intercept(context, mRouteRequest.getUri(), mRouteRequest.getExtras())) {
                    callback(RouteResult.INTERCEPTED, "Intercepted by global interceptor.");
                    return null;
                }
            }
        }

        List<AbsMatcher> matchers = MatcherRegistry.getMatcher();
        if (matchers.isEmpty()) {
            callback(RouteResult.FAILED, "The MatcherRegistry contains no Matcher.");
            return null;
        }

        Set<Map.Entry<String, Class<?>>> entries = AptHub.routeTable.entrySet();

        for (AbsMatcher matcher : matchers) {
            if (AptHub.routeTable.isEmpty()) { // implicit totally.
                if (matcher.match(context, mRouteRequest.getUri(), null, mRouteRequest)) {
                    RLog.i("Caught by " + matcher.getClass().getCanonicalName());
                    Object intent = matcher.generate(context, mRouteRequest.getUri(), null);
                    if (intent instanceof Intent) {
                        assembleIntent((Intent) intent);
                        return (Intent) intent;
                    }
                    return null;
                }
            } else {
                for (Map.Entry<String, Class<?>> entry : entries) {
                    if (matcher.match(context, mRouteRequest.getUri(), entry.getKey(), mRouteRequest)) {
                        RLog.i("Caught by " + matcher.getClass().getCanonicalName());
                        // Ignore implicit intent.
                        if (!(matcher instanceof AbsImplicitMatcher) &&
                                intercept(context, entry.getValue())) {
                            return null;
                        }
                        Object intent = matcher.generate(context, mRouteRequest.getUri(), entry.getValue());
                        if (intent instanceof Intent) {
                            assembleIntent((Intent) intent);
                            return (Intent) intent;
                        }
                        return null;
                    }
                }
            }
        }

        callback(RouteResult.FAILED, "Can not find an Activity that matches the given uri: "
                + mRouteRequest.getUri());
        return null;
    }

    private void assembleIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        if (mRouteRequest.getExtras() != null && !mRouteRequest.getExtras().isEmpty()) {
            intent.putExtras(mRouteRequest.getExtras());
        }
        if (mRouteRequest.getFlags() != 0) {
            intent.addFlags(mRouteRequest.getFlags());
        }
    }

    private boolean intercept(Context context, Class<?> target) {
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
                if (interceptor != null && interceptor.intercept(context, mRouteRequest.getUri(),
                        mRouteRequest.getExtras())) {
                    callback(RouteResult.INTERCEPTED,
                            String.format("Intercepted by interceptor: %s.", name));
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void go(Context context) {
        Intent intent = getIntent(context);
        if (intent == null) {
            return;
        }

        Bundle options = mRouteRequest.getActivityOptions() == null ?
                null : mRouteRequest.getActivityOptions().toBundle();

        if (context instanceof Activity) {
            ActivityCompat.startActivityForResult((Activity) context, intent,
                    mRouteRequest.getRequestCode(), options);

            if (mRouteRequest.getEnterAnim() != 0 && mRouteRequest.getExitAnim() != 0) {
                // Add transition animation.
                ((Activity) context).overridePendingTransition(
                        mRouteRequest.getEnterAnim(), mRouteRequest.getExitAnim());
            }
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ContextCompat.startActivity(context, intent, options);
        }

        callback(RouteResult.SUCCEED, null);
    }


    @Override
    public void go(Fragment fragment) {
        FragmentActivity activity = fragment.getActivity();
        Context context = fragment.getContext();
        Intent intent = getIntent(activity != null ? activity : context);
        if (intent == null) {
            return;
        }
        Bundle options = mRouteRequest.getActivityOptions() == null ?
                null : mRouteRequest.getActivityOptions().toBundle();

        if (mRouteRequest.getRequestCode() < 0) {
            fragment.startActivity(intent, options);
        } else {
            fragment.startActivityForResult(intent, mRouteRequest.getRequestCode(), options);
        }
        if (activity != null && mRouteRequest.getEnterAnim() != 0 && mRouteRequest.getExitAnim() != 0) {
            // Add transition animation.
            activity.overridePendingTransition(
                    mRouteRequest.getEnterAnim(), mRouteRequest.getExitAnim());
        }

        callback(RouteResult.SUCCEED, null);
    }


    @Override
    public void go(android.app.Fragment fragment) {
        Activity activity = fragment.getActivity();
        Intent intent = getIntent(activity);
        if (intent == null) {
            return;
        }
        Bundle options = mRouteRequest.getActivityOptions() == null ?
                null : mRouteRequest.getActivityOptions().toBundle();

        if (mRouteRequest.getRequestCode() < 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) { // 4.1
                fragment.startActivity(intent, options);
            } else {
                fragment.startActivity(intent);
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) { // 4.1
                fragment.startActivityForResult(intent, mRouteRequest.getRequestCode(), options);
            } else {
                fragment.startActivityForResult(intent, mRouteRequest.getRequestCode());
            }
        }
        if (activity != null && mRouteRequest.getEnterAnim() != 0 && mRouteRequest.getExitAnim() != 0) {
            // Add transition animation.
            activity.overridePendingTransition(
                    mRouteRequest.getEnterAnim(), mRouteRequest.getExitAnim());
        }

        callback(RouteResult.SUCCEED, null);
    }
}
