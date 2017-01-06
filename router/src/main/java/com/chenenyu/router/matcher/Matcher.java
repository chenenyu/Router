package com.chenenyu.router.matcher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.chenenyu.router.RouteOptions;

import java.util.Map;

/**
 * Match rule. You can extends it to customize your own rule.
 * <p>
 * Created by Cheney on 2016/12/23.
 */
public abstract class Matcher implements Comparable<Matcher> {
    /**
     * Priority in matcher list.
     */
    private int priority = 10;

    public Matcher(int priority) {
        this.priority = priority;
    }

    /**
     * Determines if the given uri matches current route.
     *
     * @param context      Context.
     * @param uri          the given uri.
     * @param route        path in route table.
     * @param routeOptions {@link RouteOptions}.
     * @return True if matched, false otherwise.
     */
    public abstract boolean match(Context context, Uri uri, @Nullable String route,
                                  RouteOptions routeOptions);

    public abstract Intent onMatched(Context context, Uri uri,
                                     @Nullable Class<? extends Activity> target,
                                     RouteOptions routeOptions);

    protected Intent generateIntent(Context context, Intent intent, RouteOptions routeOptions) {
        if (routeOptions.getBundle() != null && !routeOptions.getBundle().isEmpty()) {
            intent.putExtras(routeOptions.getBundle());
        }
        if (!(context instanceof Activity)) {
            routeOptions.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        if (routeOptions.getFlags() != 0) {
            intent.addFlags(routeOptions.getFlags());
        }
        return intent;
    }

    protected void parseParams(Map<String, String> map, String query) {
        if (query != null && !query.isEmpty()) {
            String[] entries = query.split("&");
            for (String entry : entries) {
                if (entry.contains("=")) {
                    String[] kv = entry.split("=");
                    if (kv.length > 1) {
                        map.put(kv[0], kv[1]);
                    }
                }
            }
        }
    }

    /**
     * {@link android.text.TextUtils#isEmpty(CharSequence)}.
     */
    protected boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }

    @Override
    public int compareTo(@NonNull Matcher matcher) {
        if (this == matcher) {
            return 0;
        }
        if (this.priority > matcher.priority) {
            return -1;
        } else {
            return 1;
        }
    }

}
