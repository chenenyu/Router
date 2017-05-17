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
@Interceptor("SampleInterceptor")
public class SampleInterceptor implements RouteInterceptor {
    @Override
    public boolean intercept(Context context, RouteRequest routeRequest) {
        Toast.makeText(context, "Intercepted by SampleInterceptor.", Toast.LENGTH_SHORT).show();
        return true;
    }
}
