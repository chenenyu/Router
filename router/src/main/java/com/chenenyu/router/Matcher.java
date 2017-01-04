package com.chenenyu.router;

import android.content.Context;
import android.net.Uri;

/**
 * Match rule.
 * <p>
 * Created by Cheney on 2016/12/23.
 */
public interface Matcher {

    /**
     * Determines if the given uri matches current path.
     *
     * @param context      Context.
     * @param uri          the given uri.
     * @param path         path in route table.
     * @param routeOptions {@link RouteOptions}.
     * @return True if matched, false otherwise.
     */
    boolean match(Context context, Uri uri, String path, RouteOptions routeOptions);

}
