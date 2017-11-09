package com.chenenyu.router.matcher;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.chenenyu.router.RouteRequest;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Abstract matcher.
 * <p>
 * Created by chenenyu on 2016/12/23.
 */
public abstract class AbsMatcher implements Matcher {
    /**
     * Priority in matcher list.
     */
    private int priority = 10;

    public AbsMatcher(int priority) {
        this.priority = priority;
    }

    protected void parseParams(Uri uri, RouteRequest routeRequest) {
        if (uri.getQuery() != null) {
            Bundle bundle = routeRequest.getExtras();
            if (bundle == null) {
                bundle = new Bundle();
                routeRequest.setExtras(bundle);
            }

            Set<String> keys = uri.getQueryParameterNames();
            Iterator<String> keyIterator = keys.iterator();
            while (keyIterator.hasNext()) {
                String key = keyIterator.next();
                List<String> values = uri.getQueryParameters(key);
                if (values.size() > 1) {
                    bundle.putStringArray(key, values.toArray(new String[0]));
                } else if (values.size() == 1) {
                    bundle.putString(key, values.get(0));
                }
            }
        }
    }

    /**
     * {@link android.text.TextUtils#isEmpty(CharSequence)}.
     */
    protected boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }

    @Override
    public int compareTo(@NonNull Matcher matcher) {
        if (this == matcher) {
            return 0;
        }
        if (matcher instanceof AbsMatcher) {
            if (this.priority > ((AbsMatcher) matcher).priority) {
                return -1;
            } else {
                return 1;
            }
        }
        return matcher.compareTo(this);
    }

}
