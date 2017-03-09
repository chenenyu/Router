package com.chenenyu.router;

import android.content.Context;
import android.net.Uri;

import com.chenenyu.router.matcher.Matcher;
import com.chenenyu.router.matcher.MatcherRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry class.
 * <p>
 * Created by Cheney on 2016/12/20.
 */
@SuppressWarnings("unused")
public class Router {
    private static List<RouteInterceptor> mGlobalInterceptors = new ArrayList<>();

    public static void initialize(Context context) {
        RealRouter.get().init();
    }

    public static void openLog() {
        RLog.openLog();
    }

    public static RealRouter build(String path) {
        return build(path == null ? null : Uri.parse(path));
    }

    public static RealRouter build(Uri uri) {
        return RealRouter.get().build(uri);
    }

    public static void addRouteTable(RouteTable routeTable) {
        RealRouter.get().addRouteTable(routeTable);
    }

    /**
     * Deprecated.<p>
     * Use {@link #addGlobalInterceptor(RouteInterceptor)} instead.<p>
     * To be removed in a future release.
     */
    @Deprecated
    public static void addRouteInterceptor(RouteInterceptor routeInterceptor) {
        mGlobalInterceptors.add(routeInterceptor);
    }

    /**
     * Deprecated.<p>
     * Use {@link #getGlobalInterceptors()} instead.<p>
     * To be removed in a future release.
     */
    @Deprecated
    public static List<RouteInterceptor> getRouteInterceptors() {
        return mGlobalInterceptors;
    }

    public static void addGlobalInterceptor(RouteInterceptor routeInterceptor) {
        mGlobalInterceptors.add(routeInterceptor);
    }

    public static List<RouteInterceptor> getGlobalInterceptors() {
        return mGlobalInterceptors;
    }

    public static void registerMatcher(Matcher matcher) {
        MatcherRegistry.register(matcher);
    }

    public static void clearMatcher() {
        MatcherRegistry.clear();
    }
}
