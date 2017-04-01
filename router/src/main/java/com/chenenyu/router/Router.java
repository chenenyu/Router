package com.chenenyu.router;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.chenenyu.router.matcher.Matcher;
import com.chenenyu.router.matcher.MatcherRegistry;
import com.chenenyu.router.util.ProcessUtils;
import com.chenenyu.router.util.RLog;

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
        initialize(context, false);
    }

    /**
     * Initialize router.
     *
     * @param context    Context
     * @param debuggable boolean
     */
    public static void initialize(Context context, boolean debuggable) {
        if (debuggable) {
            setDebuggable(true);
        }
        if (ProcessUtils.isMainProcess()) {
            AptHub.init();
        } else {
            Intent binderIntent = new Intent(context, RouterService.class);
            context.bindService(binderIntent, LocalRouter.getInstance().mServiceConnection,
                    Context.BIND_AUTO_CREATE);
        }
    }

    public static boolean isDebuggable() {
        return sDebuggable;
    }

    public static void setDebuggable(boolean debuggable) {
        RLog.showLog(debuggable);
        sDebuggable = debuggable;
    }

    public static IRouter build(String path) {
        return build(path == null ? null : Uri.parse(path));
    }

    public static IRouter build(Uri uri) {
        if (ProcessUtils.isMainProcess()) {
            return MainRouter.getInstance().build(uri);
        }
        return LocalRouter.getInstance().build(uri);
    }

    /**
     * Custom router table.
     */
    public static void addRouteTable(RouteTable routeTable) {
        if (ProcessUtils.isMainProcess()) {
            MainRouter.getInstance().addRouteTable(routeTable);
        } else {
            RLog.w("`addRouteTable` only works in main process.");
        }
    }

    /**
     * Global interceptor.
     */
    public static void addGlobalInterceptor(RouteInterceptor routeInterceptor) {
        if (ProcessUtils.isMainProcess()) {
            sGlobalInterceptors.add(routeInterceptor);
        } else {
            RLog.w("`addGlobalInterceptor` only works in main process.");
        }
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
    public static void registerMatcher(Matcher matcher) {
        if (ProcessUtils.isMainProcess()) {
            MatcherRegistry.register(matcher);
        } else {
            RLog.w("`registerMatcher` only works in main process.");
        }
    }

    public static void clearMatcher() {
        MatcherRegistry.clear();
    }
}
