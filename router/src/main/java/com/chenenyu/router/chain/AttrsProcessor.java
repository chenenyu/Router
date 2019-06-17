package com.chenenyu.router.chain;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.chenenyu.router.RealInterceptorChain;
import com.chenenyu.router.RouteInterceptor;
import com.chenenyu.router.RouteRequest;
import com.chenenyu.router.RouteResponse;
import com.chenenyu.router.RouteStatus;

/**
 * Process final attrs, such as bundle.
 * <p>
 * Created by chenenyu on 2019/5/17.
 */
public class AttrsProcessor implements RouteInterceptor {
    @NonNull
    @Override
    public RouteResponse intercept(Chain chain) {
        RealInterceptorChain realChain = (RealInterceptorChain) chain;
        Object targetInstance = realChain.getTargetInstance();
        if (targetInstance instanceof Intent) {
            RouteRequest request = chain.getRequest();
            assembleIntent((Intent) targetInstance, request);
        } else if (targetInstance instanceof Fragment) {
            RouteRequest request = chain.getRequest();
            Bundle bundle = request.getExtras();
            if (bundle != null && !bundle.isEmpty()) {
                ((Fragment) targetInstance).setArguments(bundle);
            }
        }

        RouteResponse response = RouteResponse.assemble(RouteStatus.SUCCEED, null);
        if (targetInstance != null) {
            response.setResult(targetInstance);
        } else {
            response.setStatus(RouteStatus.FAILED);
        }
        return response;
    }

    private void assembleIntent(Intent intent, RouteRequest request) {
        if (request.getExtras() != null && !request.getExtras().isEmpty()) {
            intent.putExtras(request.getExtras());
        }
        if (request.getFlags() != 0) {
            intent.addFlags(request.getFlags());
        }
        if (request.getData() != null) {
            intent.setData(request.getData());
        }
        if (request.getType() != null) {
            intent.setType(request.getType());
        }
        if (request.getAction() != null) {
            intent.setAction(request.getAction());
        }
    }
}
