package com.chenenyu.router;

import android.net.Uri;

/**
 * <p>
 * Created by chenenyu on 2016/12/20.
 */
public interface RouteCallback {
    /**
     * Callback
     *
     * @param state   {@link RouteStatus}
     * @param uri     Uri
     * @param message notice msg
     */
    void callback(RouteStatus state, Uri uri, String message);
}
