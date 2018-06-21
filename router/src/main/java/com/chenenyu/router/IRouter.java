package com.chenenyu.router;

import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.AnimRes;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityOptionsCompat;

/**
 * Router interface.
 * <p>
 * Created by chenenyu on 2017/3/31.
 */
public interface IRouter {
    /**
     * Entrance.
     */
    IRouter build(Uri uri);

    IRouter build(@NonNull RouteRequest request);

    /**
     * Route request callback.
     */
    IRouter callback(RouteCallback callback);

    /**
     * Call <code>startActivityForResult</code>.
     */
    IRouter requestCode(int requestCode);

    /**
     * @see Bundle#putAll(Bundle)
     */
    IRouter with(Bundle bundle);

    /**
     * @see Bundle#putAll(PersistableBundle)
     */
    @RequiresApi(21)
    IRouter with(PersistableBundle bundle);

    /**
     * bundle.putXXX(String key, XXX value).
     */
    IRouter with(String key, Object value);

    /**
     * @see Intent#addFlags(int)
     */
    IRouter addFlags(int flags);

    /**
     * @see Intent#setData(Uri)
     */
    IRouter setData(Uri data);

    /**
     * @see Intent#setType(String)
     */
    IRouter setType(String type);

    /**
     * @see Intent#setDataAndType(Uri, String)
     */
    IRouter setDataAndType(Uri data, String type);

    /**
     * @see Intent#setAction(String)
     */
    IRouter setAction(String action);

    /**
     * @see android.app.Activity#overridePendingTransition(int, int)
     */
    IRouter anim(@AnimRes int enterAnim, @AnimRes int exitAnim);

    /**
     * {@link ActivityOptions#toBundle()} and {@link ActivityOptionsCompat#toBundle()}.
     */
    IRouter activityOptionsBundle(Bundle activityOptionsBundle);

    /**
     * Skip all the interceptors.
     */
    IRouter skipInterceptors();

    /**
     * Skip the named interceptors.
     */
    IRouter skipInterceptors(String... interceptors);

    /**
     * Add interceptors temporarily for current route request.
     */
    IRouter addInterceptors(String... interceptors);

    /**
     * Get an intent instance.
     *
     * @param source Activity or Fragment instance.
     * @return {@link Intent} instance.
     */
    Intent getIntent(@NonNull Object source);

    /**
     * Get a fragment instance.
     *
     * @param source Activity or Fragment instance.
     * @return {@link Fragment} or {@link android.support.v4.app.Fragment} instance.
     */
    Object getFragment(@NonNull Object source);

    void go(Context context, RouteCallback callback);

    void go(Context context);

    void go(Fragment fragment, RouteCallback callback);

    void go(Fragment fragment);

    void go(android.support.v4.app.Fragment fragment, RouteCallback callback);

    void go(android.support.v4.app.Fragment fragment);
}
