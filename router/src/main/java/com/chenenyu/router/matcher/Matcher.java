package com.chenenyu.router.matcher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.chenenyu.router.RouteOptions;

/**
 * Match rule.
 * <p>
 * Created by Cheney on 2017/3/7.
 */
public interface Matcher extends Comparable<Matcher> {
    /**
     * Determines if the given uri matches current route.
     *
     * @param context      Context.
     * @param uri          the given uri.
     * @param route        path in route table.
     * @param routeOptions {@link RouteOptions}.
     * @return True if matched, false otherwise.
     */
    boolean match(Context context, Uri uri, @Nullable String route, RouteOptions routeOptions);

    /**
     * Called when {@link #match(Context, Uri, String, RouteOptions)} returns true.
     *
     * @param context Context.
     * @param uri     The given uri.
     * @param target  Route target.
     * @return An intent that the matcher generated.
     */
    Intent onMatched(Context context, Uri uri, @Nullable Class<? extends Activity> target);
}
