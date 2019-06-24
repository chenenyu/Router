package com.chenenyu.router;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
    private Object targetInstance; // Intent/Fragment instance

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
        } else if (source instanceof Fragment) {
            context = ((Fragment) source).requireContext();
        }
        assert context != null;
        return context;
    }

    @Nullable
    @Override
    public Fragment getFragment() {
        return (source instanceof Fragment) ? (Fragment) source : null;
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

    public void setTargetInstance(@Nullable Object targetInstance) {
        this.targetInstance = targetInstance;
    }

    @Nullable
    public Object getTargetInstance() {
        return targetInstance;
    }

    @NonNull
    @Override
    public RouteResponse process() {
        if (interceptors.isEmpty()) {
            RouteResponse response = RouteResponse.assemble(RouteStatus.SUCCEED, null);
            if (targetInstance != null) {
                response.setResult(targetInstance);
            } else {
                response.setStatus(RouteStatus.FAILED);
            }
            return response;
        }
        RouteInterceptor interceptor = interceptors.remove(0);
        return interceptor.intercept(this);
    }

    @NonNull
    @Override
    public RouteResponse intercept() {
        return RouteResponse.assemble(RouteStatus.INTERCEPTED, null);
    }
}
