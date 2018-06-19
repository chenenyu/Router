package com.chenenyu.router.app;

import android.support.annotation.NonNull;
import android.util.Log;

import com.chenenyu.router.RouteInterceptor;
import com.chenenyu.router.RouteResponse;

/**
 * Global interceptor.
 * <p>
 * Created by chenenyu on 2017/9/11.
 */
public class GlobalInterceptor implements RouteInterceptor {
    @NonNull
    @Override
    public RouteResponse intercept(Chain chain) {
        Log.d("GlobalInterceptor", String.format("{uri: %s, interceptor: %s}",
                chain.getRequest().getUri().toString(), GlobalInterceptor.class.getName()));
        return chain.process();
    }
}
