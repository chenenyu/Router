package com.chenenyu.router;

import android.net.Uri;
import android.text.TextUtils;

/**
 * <p>
 * Created by Cheney on 2016/12/30.
 */
public class UrlMatcher implements Matcher {
    @Override
    public boolean match(Uri uri, String path) {
        if (uri.toString().equals(path)) {
            return true;
        }
        Uri route = Uri.parse(path);
        if (uri.isAbsolute() && route.isAbsolute()) { // scheme != null
            if (!uri.getScheme().equals(route.getScheme())) {
                // http != https
                return false;
            }
            if (TextUtils.isEmpty(uri.getAuthority()) && TextUtils.isEmpty(route.getAuthority())) {
                // host1 == host2 == 空
                return true;
            }
            if (!TextUtils.isEmpty(uri.getAuthority()) && !TextUtils.isEmpty(route.getAuthority())
                    && uri.getAuthority().equals(route.getAuthority())) {
                // google.com == google.com (include port)
                if (!cutSlash(uri.getPath()).equals(cutSlash(route.getPath()))) {
                    // /main != /home
                    return false;
                }
                // TODO: 2016/12/30 check query
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
