package com.chenenyu.router.matcher;

import android.support.annotation.NonNull;

import java.util.Map;

/**
 * Abstract matcher.
 * <p>
 * Created by Cheney on 2016/12/23.
 */
public abstract class AbsMatcher implements Matcher {
    /**
     * Priority in matcher list.
     */
    private int priority = 10;

    public AbsMatcher(int priority) {
        this.priority = priority;
    }

    protected void parseParams(Map<String, String> map, String query) {
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
