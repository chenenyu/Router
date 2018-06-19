package com.chenenyu.router.app;

import android.support.annotation.NonNull;
import android.widget.Toast;

import com.chenenyu.router.RouteInterceptor;
import com.chenenyu.router.RouteResponse;
import com.chenenyu.router.annotation.Interceptor;

/**
 * 自定义拦截器，通过注解指定name，就可以在Route中引用
 * <p>
 * Created by Cheney on 2017/3/6.
 */
@Interceptor("AInterceptor")
public class AInterceptor implements RouteInterceptor {
    @NonNull
    @Override
    public RouteResponse intercept(Chain chain) {
        Toast.makeText(chain.getContext(), String.format("Intercepted: {uri: %s, interceptor: %s}",
                chain.getRequest().getUri().toString(), AInterceptor.class.getName()),
                Toast.LENGTH_LONG).show();
        return chain.intercept();
    }
}
