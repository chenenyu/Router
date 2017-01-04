package com.chenenyu.router;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;

/**
 * Support for implicit intent.
 * <p>
 * Created by zhangleilei on 04/01/2017.
 */
public class SchemeMatcher implements Matcher {
    @Override
    public boolean match(Context context, Uri uri, String path, RouteOptions routeOptions) {
        if (context.getPackageManager().resolveActivity(
                new Intent().setData(uri), PackageManager.MATCH_DEFAULT_ONLY) != null) {
            if (uri.getQuery() != null) {
                Map<String, String> map = new HashMap<>();
                parseParams(map, uri.getQuery());
                Bundle bundle = routeOptions.getBundle();
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    if (bundle == null) {
                        bundle = new Bundle();
                        routeOptions.setBundle(bundle);
                    }
                    bundle.putString(entry.getKey(), entry.getValue());
                }
            }
            return true;
        }
        return false;
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
