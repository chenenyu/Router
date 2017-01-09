package com.chenenyu.router.matcher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.chenenyu.router.RouteOptions;

/**
 * Internal default matcher.
 * <p>
 * Created by Cheney on 2016/12/23.
 */
public class SimpleMatcher extends Matcher {

    public SimpleMatcher(int priority) {
        super(priority);
    }

    @Override
    public boolean match(Context context, Uri uri, @Nullable String route, RouteOptions routeOptions) {
        return !isEmpty(route) && uri.toString().equals(route);
    }

    @Override
    public Intent onMatched(Context context, Uri uri, @Nullable Class<? extends Activity> target,
                            RouteOptions routeOptions) {
        if (target == null) {
            return null;
        }
        return new Intent(context, target);
    }

}
