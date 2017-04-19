package com.chenenyu.router;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.chenenyu.router.matcher.AbsImplicitMatcher;
import com.chenenyu.router.matcher.Matcher;
import com.chenenyu.router.matcher.MatcherRegistry;
import com.chenenyu.router.util.AppUtils;
import com.chenenyu.router.util.RLog;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Router for main process.
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
            routeTable.handleActivityTable(AptHub.activityTable);
        }
    }

    /**
     * Generate route response for the request.
     *
     * @param routeRequest {@link RouteRequest}
     * @return {@link RouteResponse}
     */
    RouteResponse route(RouteRequest routeRequest) {
        mRouteRequest = routeRequest;
        mRouteResponse = new RouteResponse();
        Intent data = getIntent(AppUtils.INSTANCE);
        if (data != null) {
            mRouteResponse.setResult(RouteResult.SUCCEED);
            mRouteResponse.setMsg(null);
        }
        mRouteResponse.setData(data);
        return mRouteResponse;
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

        List<Matcher> matchers = MatcherRegistry.getMatcher();
        if (matchers.isEmpty()) {
            callback(RouteResult.FAILED, "The MatcherRegistry contains no Matcher.");
            return null;
        }

        Set<Map.Entry<String, Class<? extends Activity>>> entries = AptHub.activityTable.entrySet();

        for (Matcher matcher : matchers) {
            if (AptHub.activityTable.isEmpty()) { // implicit totally.
                if (matcher.match(context, mRouteRequest.getUri(), null, mRouteRequest)) {
                    RLog.i("Caught by " + matcher.getClass().getCanonicalName());
                    Intent intent = matcher.onMatched(context, mRouteRequest.getUri(), null);
                    assembleIntent(intent);
                    return intent;
                }
            } else {
                for (Map.Entry<String, Class<? extends Activity>> entry : entries) {
                    if (matcher.match(context, mRouteRequest.getUri(), entry.getKey(), mRouteRequest)) {
                        RLog.i("Caught by " + matcher.getClass().getCanonicalName());
                        // Ignore implicit intent.
                        if (!(matcher instanceof AbsImplicitMatcher) &&
                                intercept(context, entry.getValue())) {
                            return null;
                        }
                        Intent intent = matcher.onMatched(context, mRouteRequest.getUri(), entry.getValue());
                        assembleIntent(intent);
                        return intent;
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

}
