package com.chenenyu.router;

import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry class.
 * <p>
 * Created by Cheney on 2016/12/20.
 */
public class Router {
    private static List<RouteInterceptor> mRouteInterceptors = new ArrayList<>();
    private static List<Matcher> mMatchers = new ArrayList<>();

    public static RealRouter build(String path) {
        return build(path == null ? null : Uri.parse(path));
    }

    public static RealRouter build(Uri uri) {
        return RealRouter.get().build(uri);
    }

    public static void addRouteTable(RouteTable routeTable) {
        RealRouter.get().addRouteTable(routeTable);
    }

    public static void addRouteInterceptor(RouteInterceptor routeInterceptor) {
        mRouteInterceptors.add(routeInterceptor);
    }

    public static void removeRouteInterceptor(RouteInterceptor routeInterceptor) {
        mRouteInterceptors.remove(routeInterceptor);
    }

    public static List<RouteInterceptor> getRouteInterceptors() {
        return mRouteInterceptors;
    }

    public static void clearRouteInterceptors() {
        mRouteInterceptors.clear();
    }

    public static void addMatcher(Matcher matcher) {
        mMatchers.add(matcher);
    }

    public static void removeMatcher(Matcher matcher) {
        mMatchers.remove(matcher);
    }

    public static List<Matcher> getMatchers() {
        return mMatchers;
    }

    public static void clearMatchers() {
        mMatchers.clear();
    }
}
