package com.chenenyu.router;

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
public class Router {
    private static List<RouteInterceptor> mRouteInterceptors = new ArrayList<>();

    public static RealRouter build(String path) {
        return build(path == null ? null : Uri.parse(path));
    }

    public static RealRouter build(Uri uri) {
        return RealRouter.get().build(uri);
    }

    public static void openLog() {
        RLog.openLog();
    }

    public static void addRouteTable(RouteTable routeTable) {
        RealRouter.get().addRouteTable(routeTable);
    }

    public static void addRouteInterceptor(RouteInterceptor routeInterceptor) {
        mRouteInterceptors.add(routeInterceptor);
    }

    public static List<RouteInterceptor> getRouteInterceptors() {
        return mRouteInterceptors;
    }

    public static void registerMatcher(Matcher matcher) {
        MatcherRegistry.register(matcher);
    }

    public static void clearMatcher() {
        MatcherRegistry.clear();
    }
}
