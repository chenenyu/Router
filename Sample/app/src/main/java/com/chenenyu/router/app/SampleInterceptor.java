package com.chenenyu.router.app;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.chenenyu.router.RouteInterceptor;
import com.chenenyu.router.annotation.Interceptor;

/**
 * 自定义拦截器，通过注解指定name，就可以在Route中引用
 * <p>
 * Created by Cheney on 2017/3/6.
 */
@Interceptor("SampleInterceptor")
public class SampleInterceptor implements RouteInterceptor {
    @Override
    public boolean intercept(Context context, @NonNull Uri uri, @Nullable Bundle extras) {
        Toast.makeText(context, "Intercepted by SampleInterceptor.", Toast.LENGTH_SHORT).show();
        return true;
    }
}
