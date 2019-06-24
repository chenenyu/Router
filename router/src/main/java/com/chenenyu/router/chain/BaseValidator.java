package com.chenenyu.router.chain;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.chenenyu.router.RouteInterceptor;
import com.chenenyu.router.RouteRequest;
import com.chenenyu.router.RouteResponse;
import com.chenenyu.router.RouteStatus;

/**
 * Created by chenenyu on 2018/6/14.
 */
public class BaseValidator implements RouteInterceptor {
    @NonNull
    @Override
    public RouteResponse intercept(Chain chain) {
        RouteRequest request = chain.getRequest();
        if (request.getUri() == null) {
            return RouteResponse.assemble(RouteStatus.FAILED, "uri == null.");
        }

        Context context = null;
        if (chain.getSource() instanceof Context) {
            context = (Context) chain.getSource();
        } else if (chain.getSource() instanceof Fragment) {
            if (Build.VERSION.SDK_INT >= 23) {
                context = ((Fragment) chain.getSource()).getContext();
            } else {
                context = ((Fragment) chain.getSource()).getActivity();
            }
        }
        if (context == null) {
            return RouteResponse.assemble(RouteStatus.FAILED, "Can't retrieve context from source.");
        }

        return chain.process();
    }
}
