package com.chenenyu.router.matcher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.chenenyu.router.RouteRequest;

/**
 * Support for <strong>implicit intent</strong> exclude scheme "http(s)",
 * cause we may want to resolve them in custom matcher, such as {@link SchemeMatcher},
 * or {@link BrowserMatcher}.
 * <p>
 * Created by chenenyu on 2017/01/08.
 */
public class ImplicitMatcher extends AbsImplicitMatcher {
    public ImplicitMatcher(int priority) {
        super(priority);
    }

    @Override
    public boolean match(Context context, Uri uri, @Nullable String route, RouteRequest routeRequest) {
        if (uri.toString().toLowerCase().startsWith("http://")
                || uri.toString().toLowerCase().startsWith("https://")) {
            return false;
        }
        ResolveInfo resolveInfo = context.getPackageManager().resolveActivity(
                new Intent(Intent.ACTION_VIEW, uri), PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfo != null) {
            // bundle parser
            if (uri.getQuery() != null) {
                parseParams(uri, routeRequest);
            }
            return true;
        }
        return false;
    }
}
