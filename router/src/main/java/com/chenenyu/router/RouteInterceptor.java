package com.chenenyu.router;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

/**
 * Interceptor before route.
 * <p>
 * Created by chenenyu on 2016/12/20.
 */
public interface RouteInterceptor {
    @NonNull
    RouteResponse intercept(Chain chain);

    interface Chain {
        @NonNull
        RouteRequest getRequest();

        @NonNull
        Object getSource();

        @NonNull
        Context getContext();

        @Nullable
        android.app.Fragment getFragment();

        @Nullable
        Fragment getFragmentV4();

        @NonNull
        RouteResponse process();
    }
}
