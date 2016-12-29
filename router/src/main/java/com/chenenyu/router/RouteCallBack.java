package com.chenenyu.router;

import android.net.Uri;

/**
 * <p>
 * Created by Cheney on 2016/12/20.
 */
public interface RouteCallBack {
    void succeed(Uri uri);

    void error(Uri uri, String message);
}
