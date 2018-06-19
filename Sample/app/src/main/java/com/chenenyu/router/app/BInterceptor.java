package com.chenenyu.router.app;

import android.support.annotation.NonNull;
import android.widget.Toast;

import com.chenenyu.router.RouteInterceptor;
import com.chenenyu.router.RouteResponse;
import com.chenenyu.router.annotation.Interceptor;


/**
 * Created by chenenyu on 2018/5/18.
 */
@Interceptor("BInterceptor")
public class BInterceptor implements RouteInterceptor {
    @NonNull
    @Override
    public RouteResponse intercept(Chain chain) {
        Toast.makeText(chain.getContext(), String.format("Intercepted: {uri: %s, interceptor: %s}",
                chain.getRequest().getUri().toString(), BInterceptor.class.getName()),
                Toast.LENGTH_LONG).show();
        return chain.intercept();
    }
}
