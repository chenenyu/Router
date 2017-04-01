package com.chenenyu.router;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.AnimRes;

/**
 * Router interface.
 * <p>
 * Created by Cheney on 2017/3/31.
 */
public interface IRouter {
    IRouter build(Uri uri);

    IRouter callback(RouteCallback callback);

    IRouter requestCode(int requestCode);

    IRouter extras(Bundle bundle);

    IRouter addFlags(int flags);

    IRouter anim(@AnimRes int enterAnim, @AnimRes int exitAnim);

    IRouter skipInterceptors();

    Intent getIntent(Context context);

    void go(Context context);
}
