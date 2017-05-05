package com.chenenyu.router.matcher;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.chenenyu.router.RouteRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Standard scheme matcher. It matches scheme, authority(host, port) and path(if offered),
 * then transfers the query part(if offered) to bundle/arguments.
 * <p>
 * If you configured a route like this:
 * <code>
 * <p>
 * -> @Route("http://example.com/user")
 * <p>
 * </code>
 * Then <a href="">http://example.com/user</a> will match this route,
 * <a href="">http://example.com/user?id=9527&status=0</a> also does and puts bundles for you:
 * <code>
 * <p>
 * bundle.putString("id", "9527");
 * <br>
 * bundle.putString("status", "0");
 * <br>
 * intent.putExtras(bundle);  or fragment.setArguments(bundle);
 * <p>
 * </code>
 * <p>
 * Created by Cheney on 2016/12/30.
 */
public class SchemeMatcher extends AbsExplicitMatcher {
    public SchemeMatcher(int priority) {
        super(priority);
    }

    @Override
    public boolean match(Context context, Uri uri, @Nullable String route, RouteRequest routeRequest) {
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
                if (uri.getQuery() != null) {
                    // parse entry from given uri.
                    Map<String, String> params = new HashMap<>();
                    parseParams(params, uri.getQuery());

                    if (!params.isEmpty()) {
                        Bundle bundle = routeRequest.getExtras();
                        if (bundle == null) {
                            bundle = new Bundle();
                            routeRequest.setExtras(bundle);
                        }
                        for (Map.Entry<String, String> entry : params.entrySet()) {
                            bundle.putString(entry.getKey(), entry.getValue());
                        }
                    }
                }
                return true;
            }
        }
        return false;
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
