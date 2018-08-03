package com.chenenyu.router;

import android.net.Uri;

import com.chenenyu.router.matcher.AbsMatcher;
import com.chenenyu.router.template.RouteTable;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry class.
 * <p>
 * Created by chenenyu on 2016/12/20.
 */
public class Router {
    /**
     * You can get the raw uri in target page by call <code>intent.getStringExtra(Router.RAW_URI)</code>.
     */
    public static final String RAW_URI = "raw_uri";

    private static final List<RouteInterceptor> sGlobalInterceptors = new ArrayList<>();


    public static IRouter build(String path) {
        return build(path == null ? null : Uri.parse(path));
    }

    public static IRouter build(Uri uri) {
        return RealRouter.getInstance().build(uri);
    }

    public static IRouter build(RouteRequest request) {
        return RealRouter.getInstance().build(request);
    }

    /**
     * Custom route table.
     */
    public static void handleRouteTable(RouteTable routeTable) {
        if (routeTable != null) {
            routeTable.handle(AptHub.routeTable);
        }
    }

    /**
     * Auto inject params from bundle.
     *
     * @param obj Instance of Activity or Fragment.
     */
    public static void injectParams(Object obj) {
        AptHub.injectParams(obj);
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
