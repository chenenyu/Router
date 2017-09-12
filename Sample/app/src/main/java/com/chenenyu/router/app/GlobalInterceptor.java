package com.chenenyu.router.app;

import android.content.Context;
import android.util.Log;

import com.chenenyu.router.RouteInterceptor;
import com.chenenyu.router.RouteRequest;

/**
 * Global interceptor.
 * <p>
 * Created by chenenyu on 2017/9/11.
 */
public class GlobalInterceptor implements RouteInterceptor {
    @Override
    public boolean intercept(Context context, RouteRequest routeRequest) {
        Log.d("GlobalInterceptor", String.format("Intercepted: {uri: %s, interceptor: %s}",
                routeRequest.getUri().toString(), GlobalInterceptor.class.getName()));
        return false;
    }
}
