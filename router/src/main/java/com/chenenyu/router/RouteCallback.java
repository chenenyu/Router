package com.chenenyu.router;

import android.net.Uri;

import java.io.Serializable;

/**
 * <p>
 * Created by chenenyu on 2016/12/20.
 */
public interface RouteCallback extends Serializable {
    /**
     * Callback
     *
     * @param state   {@link RouteResult}
     * @param uri     Uri
     * @param message notice msg
     */
    void callback(RouteResult state, Uri uri, String message);
}
