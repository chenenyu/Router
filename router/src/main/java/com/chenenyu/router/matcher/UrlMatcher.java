package com.chenenyu.router.matcher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.chenenyu.router.RouteOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * Standard url matcher. It matches scheme, authority(host, port) and path,
 * then transfers the query part(if offered) to bundle.
 * <p>
 * If you configured a route like this:
 * <code>
 * <p>
 * -> @Route("http://example.com/user?{id}&{status}")
 * <p>
 * </code>
 * Then <a href="">http://example.com/user</a> will match this route,
 * <a href="">http://example.com/user?id=9527&status=0</a> also does and with bundle:
 * <code>
 * <p>
 * bundle.putString("id", "9527");
 * <br>
 * bundle.putString("status", "0");
 * <p>
 * </code>
 * <p>
 * Created by Cheney on 2016/12/30.
 */
public class UrlMatcher extends Matcher {
    public UrlMatcher(int priority) {
        super(priority);
    }

    @Override
    public boolean match(Context context, Uri uri, @Nullable String route, RouteOptions routeOptions) {
        if (isEmpty(route)) {
            return false;
        }
        Uri routeUri = Uri.parse(route);
        if (uri.isAbsolute() && routeUri.isAbsolute()) { // scheme != null
            if (!uri.getScheme().equals(routeUri.getScheme())) {
                // http != https
                return false;
            }
            if (isEmpty(uri.getAuthority()) && isEmpty(routeUri.getAuthority())) {
                // host1 == host2 == empty
                return true;
            }
            // google.com == google.com (include port)
            if (!isEmpty(uri.getAuthority()) && !isEmpty(routeUri.getAuthority())
                    && uri.getAuthority().equals(routeUri.getAuthority())) {
                if (!cutSlash(uri.getPath()).equals(cutSlash(routeUri.getPath()))) {
                    return false;
                }

                // bundle parser
                if (routeUri.getQuery() != null && uri.getQuery() != null) {
                    // parse entry from given uri.
                    Map<String, String> params = new HashMap<>();
                    parseParams(params, uri.getQuery());

                    String[] placeholders = routeUri.getQuery().split("&");
                    for (String placeholder : placeholders) {
                        if (placeholder.startsWith("{") && placeholder.endsWith("}")) {
                            placeholder = placeholder.substring(1, placeholder.length() - 1);
                            if (params.containsKey(placeholder)) {
                                Bundle bundle = routeOptions.getBundle();
                                if (bundle == null) {
                                    bundle = new Bundle();
                                    routeOptions.setBundle(bundle);
                                }
                                bundle.putString(placeholder, params.get(placeholder));
                            }
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public Intent onMatched(Context context, Uri uri, @Nullable Class<? extends Activity> target,
                            RouteOptions routeOptions) {
        if (target == null) {
            return null;
        }
        return new Intent(context, target);
    }

    /**
     * 剔除path头部和尾部的斜杠/
     *
     * @param path 路径
     * @return 无/的路径
     */
    private String cutSlash(String path) {
        if (path.startsWith("/")) {
            return cutSlash(path.substring(1));
        }
        if (path.endsWith("/")) {
            return cutSlash(path.substring(0, path.length() - 1));
        }
        return path;
    }

}
