package com.chenenyu.router.matcher;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.chenenyu.router.RouteRequest;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Chen
 * @date 2017/12/9
 */

public class RestfulParamsMatcher extends AbsExplicitMatcher {
    private static final String PARAM_VALUE = "([a-zA-Z0-9_#'!+%~,\\-\\.\\@\\$\\:]+)";
    private static final String PARAM = "([a-zA-Z][a-zA-Z0-9_-]*)";
    private static final String PARAM_REGEX = "%7B(" + PARAM + ")%7D";
    private static final Pattern PARAM_PATTERN = Pattern.compile(PARAM_REGEX);


    private Pattern mRegex;
    private HashMap<String, Set<String>> mRouteParamsMap;

    public RestfulParamsMatcher(int priority) {
        super(priority);
        mRouteParamsMap = new HashMap<>();
    }

    @Override
    public boolean match(Context context, Uri uri, @Nullable String route, RouteRequest routeRequest) {
        Uri routerUri = Uri.parse(route);
        if (mRouteParamsMap.get(route) == null) {
            mRouteParamsMap.put(route, parseParameters(routerUri));
        }
        Set<String> parameters = mRouteParamsMap.get(route);
        if (parameters == null) {
            return false;
        }
        String schemeHostAndPath = schemeHostAndPath(routerUri);
        this.mRegex = Pattern.compile(schemeHostAndPath.replaceAll(PARAM_REGEX, PARAM_VALUE) + "$");
        Iterator<String> paramsIterator = parameters.iterator();
        Matcher matcher = mRegex.matcher(schemeHostAndPath(uri));
        int i = 1;

        if (matcher.matches()) {
            Bundle bundle = routeRequest.getExtras();
            while (paramsIterator.hasNext()) {
                String key = paramsIterator.next();
                String value = matcher.group(i++);
                if (value != null && !"".equals(value.trim())) {
                    bundle.putString(key, value);
                }
            }

            if (uri.getQuery() != null) {
                parseParams(uri, routeRequest);
            }
            return true;
        } else {
            return false;
        }
    }

    private static Set<String> parseParameters(Uri uri) {
        Matcher matcher = PARAM_PATTERN.matcher(uri.getHost() + getEncodePath(uri));
        Set<String> patterns = new LinkedHashSet<>();
        while (matcher.find()) {
            patterns.add(matcher.group(1));
        }
        return patterns;
    }

    private static String schemeHostAndPath(Uri uri) {
        return uri.getScheme() + "://" + uri.getHost() + getEncodePath(uri);
    }

    private static String getEncodePath(Uri uri) {
        String path = uri.getPath();
        StringBuilder decodePath = new StringBuilder();
        if (!TextUtils.isEmpty(path)) {
            String[] pathArray = path.split("/");
            for (String temp : pathArray
                    ) {
                if (!TextUtils.isEmpty(temp)) {
                    decodePath.append("/");
                    decodePath.append(Uri.encode(temp));
                }
            }
        }
        return decodePath.toString();
    }
}

