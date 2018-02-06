package com.chenenyu.router;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.AnimRes;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;

/**
 * Router interface.
 * <p>
 * Created by chenenyu on 2017/3/31.
 */
public interface IRouter {
    IRouter build(Uri uri);

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
     * {@link ActivityOptionsCompat}.
     */
    IRouter activityOptions(ActivityOptionsCompat activityOptions);

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

    Intent getIntent(Context context);

    Object getFragment(Context context);

    void go(Context context, RouteCallback callback);

    void go(Context context);

    void go(Fragment fragment, RouteCallback callback);

    void go(Fragment fragment);

    void go(android.app.Fragment fragment, RouteCallback callback);

    void go(android.app.Fragment fragment);

    /**
     * Call method by a {@link MethodCallable} instance. The method can be static or not.
     * If the method has parameters, each param's type must be {@link String} and
     * all the params must be annotated by {@link com.chenenyu.router.annotation.InjectParam}.
     *
     * @param context  Context
     * @param callable A {@link MethodCallable} instance.
     * @return True if find the method and invoke successfully, false otherwise.
     */
    boolean go(Context context, MethodCallable callable);

    /**
     * Call method by a subclass of {@link MethodCallable}. <strong>The method must be static.</strong>
     * If the method has parameters, each param's type must be {@link String} and
     * all the params must be annotated by {@link com.chenenyu.router.annotation.InjectParam}.
     *
     * @param context Context
     * @param clz     Class that implements {@link MethodCallable} interface.
     * @return True if find the method and invoke successfully, false otherwise.
     */
    boolean go(Context context, Class<? extends MethodCallable> clz);
}
