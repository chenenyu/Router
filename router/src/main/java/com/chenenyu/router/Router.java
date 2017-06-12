package com.chenenyu.router;

import android.content.Context;
import android.net.Uri;

import com.chenenyu.router.matcher.AbsMatcher;
import com.chenenyu.router.util.RLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry class.
 * <p>
 * Created by Cheney on 2016/12/20.
 */
public class Router {
    private static List<RouteInterceptor> sGlobalInterceptors = new ArrayList<>();

    private static boolean sDebuggable = false;

    public static void initialize(Context context) {
        initialize(context, false);
    }

    /**
     * Initialize router.
     *
     * @param context    placeholder for a future usage.
     * @param debuggable {@link #setDebuggable(boolean)}.
     */
    public static void initialize(Context context, boolean debuggable) {
        if (debuggable) {
            setDebuggable(true);
        }
        AptHub.init();
    }

    public static boolean isDebuggable() {
        return sDebuggable;
    }

    public static void setDebuggable(boolean debuggable) {
        sDebuggable = debuggable;
        RLog.showLog(debuggable);
    }

    public static IRouter build(String path) {
        return build(path == null ? null : Uri.parse(path));
    }

    public static IRouter build(Uri uri) {
        return MainRouter.getInstance().build(uri);
    }

    /**
     * Use {@link #handleRouteTable(RouteTable)} instead.
     * <p>
     * This method will be <strong>removed</strong> in a future release.
     */
    @Deprecated
    public static void addRouteTable(RouteTable routeTable) {
        MainRouter.getInstance().handleRouteTable(routeTable);
    }

    /**
     * Custom router table.
     */
    public static void handleRouteTable(RouteTable routeTable) {
        MainRouter.getInstance().handleRouteTable(routeTable);
    }

    /**
     * Global interceptor.
     */
    public static void addGlobalInterceptor(RouteInterceptor routeInterceptor) {
        sGlobalInterceptors.add(routeInterceptor);
    }

    public static List<RouteInterceptor> getGlobalInterceptors() {
        return sGlobalInterceptors;
    }

    /**
     * Register your own matcher.
     *
     * @see com.chenenyu.router.matcher.AbsExplicitMatcher
     * @see com.chenenyu.router.matcher.AbsImplicitMatcher
     */
    public static void registerMatcher(AbsMatcher matcher) {
        MatcherRegistry.register(matcher);
    }

    public static void clearMatcher() {
        MatcherRegistry.clear();
    }
}
