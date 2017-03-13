package com.chenenyu.router.matcher;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.chenenyu.router.RouteOptions;

/**
 * Absolutely matcher.
 * <p>
 * Created by Cheney on 2016/12/23.
 */
public class DirectMatcher extends AbsExplicitMatcher {

    public DirectMatcher(int priority) {
        super(priority);
    }

    @Override
    public boolean match(Context context, Uri uri, @Nullable String route, RouteOptions routeOptions) {
        return !isEmpty(route) && uri.toString().equals(route);
    }

}
