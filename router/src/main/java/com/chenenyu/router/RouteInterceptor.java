package com.chenenyu.router;

import android.app.Fragment;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Interceptor before route.
 * <p>
 * Created by chenenyu on 2016/12/20.
 */
public interface RouteInterceptor {
    @NonNull
    RouteResponse intercept(Chain chain);

    /**
     * Interceptor chain processor.
     */
    interface Chain {
        /**
         * Get current RouteRequest object.
         */
        @NonNull
        RouteRequest getRequest();

        /**
         * Get source object, activity or fragment instance.
         */
        @NonNull
        Object getSource();

        @NonNull
        Context getContext();

        @Nullable
        Fragment getFragment();

        @Nullable
        android.support.v4.app.Fragment getFragmentV4();

        /**
         * Continue to process this route request.
         */
        @NonNull
        RouteResponse process();

        /**
         * Intercept this route request.
         */
        @NonNull
        RouteResponse intercept();
    }
}
