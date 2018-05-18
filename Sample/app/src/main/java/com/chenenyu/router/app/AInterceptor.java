package com.chenenyu.router.app;

import android.content.Context;
import android.widget.Toast;

import com.chenenyu.router.RouteInterceptor;
import com.chenenyu.router.RouteRequest;
import com.chenenyu.router.annotation.Interceptor;

/**
 * 自定义拦截器，通过注解指定name，就可以在Route中引用
 * <p>
 * Created by Cheney on 2017/3/6.
 */
@Interceptor("AInterceptor")
public class AInterceptor implements RouteInterceptor {
    @Override
    public boolean intercept(Object source, RouteRequest routeRequest) {
        Toast.makeText((Context) source, String.format("Intercepted: {uri: %s, interceptor: %s}",
                routeRequest.getUri().toString(), AInterceptor.class.getName()),
                Toast.LENGTH_LONG).show();
        return true;
    }
}
