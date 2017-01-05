package com.chenenyu.router;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

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
public class UrlMatcher implements Matcher {
    @Override
    public boolean match(Context context, Uri uri, String path, RouteOptions routeOptions) {
        if (uri.toString().equals(path)) {
            return true;
        }
        Uri route = Uri.parse(path);
        if (uri.isAbsolute() && route.isAbsolute()) { // scheme != null
            if (!uri.getScheme().equals(route.getScheme())) {
                // http != https
                return false;
            }
            if (isEmpty(uri.getAuthority()) && isEmpty(route.getAuthority())) {
                // host1 == host2 == empty
                return true;
            }
            // google.com == google.com (include port)
            if (!isEmpty(uri.getAuthority()) && !isEmpty(route.getAuthority())
                    && uri.getAuthority().equals(route.getAuthority())) {
                if (!cutSlash(uri.getPath()).equals(cutSlash(route.getPath()))) {
                    return false;
                }

                // bundle parser
                if (route.getQuery() != null && uri.getQuery() != null) {
                    // parse entry from given uri.
                    Map<String, String> params = new HashMap<>();
                    parseParams(params, uri.getQuery());

                    String[] placeholders = route.getQuery().split("&");
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

    /**
     * {@link android.text.TextUtils#isEmpty(CharSequence)}.
     */
    private boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }

    private void parseParams(Map<String, String> map, String query) {
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
}
