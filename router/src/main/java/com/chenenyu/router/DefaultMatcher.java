package com.chenenyu.router;

import android.content.Context;
import android.net.Uri;

/**
 * Internal default matcher.
 * <p>
 * Created by Cheney on 2016/12/23.
 */
public class DefaultMatcher implements Matcher {
    @Override
    public boolean match(Context context, Uri uri, String path, RouteOptions routeOptions) {
        return uri.toString().equals(path);
    }
}
