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
    private static List<RouteInterceptor> sGlobalInterceptors = new ArrayList<>();

    private static boolean sDebuggable = false;

    public static void initialize(Context context) {
        RealRouter.get().init();
    }

    public static boolean isDebuggable() {
        return sDebuggable;
    }

    public static void setDebuggable(boolean debuggable) {
        RLog.showLog(debuggable);
        sDebuggable = debuggable;
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

    public static void addGlobalInterceptor(RouteInterceptor routeInterceptor) {
        sGlobalInterceptors.add(routeInterceptor);
    }

    public static List<RouteInterceptor> getGlobalInterceptors() {
        return sGlobalInterceptors;
    }

    public static void registerMatcher(Matcher matcher) {
        MatcherRegistry.register(matcher);
    }

    public static void clearMatcher() {
        MatcherRegistry.clear();
    }
}
