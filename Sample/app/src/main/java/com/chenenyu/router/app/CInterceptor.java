package com.chenenyu.router.app;

import android.support.annotation.NonNull;

import com.chenenyu.router.RouteInterceptor;
import com.chenenyu.router.RouteResponse;
import com.chenenyu.router.annotation.Interceptor;

/**
 * 自定义拦截器，通过注解指定name，就可以在Route中引用
 * <p>
 * Created by Cheney on 2017/3/6.
 */
@Interceptor("CInterceptor")
public class CInterceptor implements RouteInterceptor {
    public static String extraKey = "cInterceptorKey";

    @NonNull
    @Override
    public RouteResponse intercept(Chain chain) {
        chain.getRequest().getExtras().putString(extraKey, "testValue");
        return chain.process();
    }
}
