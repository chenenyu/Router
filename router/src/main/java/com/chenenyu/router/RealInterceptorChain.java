package com.chenenyu.router;

import android.app.Fragment;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.chenenyu.router.chain.ResultProcessor;

import java.util.List;

/**
 * Interceptor chain processor.
 * <br>
 * Created by chenenyu on 2018/6/14.
 */
public final class RealInterceptorChain implements RouteInterceptor.Chain {
    @NonNull
    private final Object source;
    @NonNull
    private final RouteRequest request;
    @NonNull
    private final List<RouteInterceptor> interceptors;
    private int index;
    @Nullable
    private Class<?> targetClass; // Intent/Fragment class
    @Nullable
    private Object targetObject; // Intent/Fragment instance

    RealInterceptorChain(@NonNull Object source,
                         @NonNull RouteRequest request,
                         @NonNull List<RouteInterceptor> interceptors) {
        this.source = source;
        this.request = request;
        this.interceptors = interceptors;
    }

    @NonNull
    @Override
    public RouteRequest getRequest() {
        return request;
    }

    @NonNull
    @Override
    public Object getSource() {
        return source;
    }

    @NonNull
    @Override
    public Context getContext() {
        Context context = null;
        if (source instanceof Context) {
            context = (Context) source;
        } else if (source instanceof android.support.v4.app.Fragment) {
            context = ((android.support.v4.app.Fragment) source).getContext();
        } else if (source instanceof Fragment) {
            if (Build.VERSION.SDK_INT >= 23) {
                context = ((Fragment) source).getContext();
            } else {
                context = ((Fragment) source).getActivity();
            }
        }
        assert context != null;
        return context;
    }

    @Nullable
    @Override
    public Fragment getFragment() {
        return (source instanceof Fragment) ? (Fragment) source : null;
    }

    @Nullable
    @Override
    public android.support.v4.app.Fragment getFragmentV4() {
        return (source instanceof android.support.v4.app.Fragment) ? (android.support.v4.app.Fragment) source : null;
    }

    @NonNull
    public List<RouteInterceptor> getInterceptors() {
        return interceptors;
    }

    @Nullable
    public Class<?> getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(@Nullable Class<?> targetClass) {
        this.targetClass = targetClass;
    }

    @Nullable
    public Object getTargetObject() {
        return targetObject;
    }

    public void setTargetObject(@Nullable Object targetObject) {
        this.targetObject = targetObject;
    }

    @NonNull
    @Override
    public RouteResponse process() {
        if (index >= interceptors.size()) {
            ResultProcessor response = new ResultProcessor();
            return response.intercept(this);
        }
        RouteInterceptor interceptor = interceptors.get(index++);
        return interceptor.intercept(this);
    }

    @NonNull
    @Override
    public RouteResponse intercept() {
        return RouteResponse.assemble(RouteStatus.INTERCEPTED, null);
    }
}
