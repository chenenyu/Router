package com.chenenyu.router.chain;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.chenenyu.router.RealInterceptorChain;
import com.chenenyu.router.RouteInterceptor;
import com.chenenyu.router.RouteRequest;
import com.chenenyu.router.RouteResponse;
import com.chenenyu.router.RouteStatus;
import com.chenenyu.router.util.RLog;

/**
 * @Author zhiqiang
 * @Email liuzhiqiang@moretickets.com
 * @Date 2019-04-26
 * @Description targetObject的Bundle赋值
 */
public class ResultProcessor implements RouteInterceptor {
    @NonNull
    @Override
    public RouteResponse intercept(Chain chain) {
        RealInterceptorChain realChain = (RealInterceptorChain) chain;

        Object targetObject = realChain.getTargetObject();
        if (targetObject instanceof Intent) {
            RouteRequest request = chain.getRequest();
            assembleIntent((Intent) targetObject, request);
        } else if (targetObject instanceof android.support.v4.app.Fragment) {
            RouteRequest request = chain.getRequest();
            Bundle bundle = request.getExtras();
            if (bundle != null && !bundle.isEmpty()) {
                ((android.support.v4.app.Fragment) targetObject).setArguments(bundle);
            }
        } else if (targetObject instanceof Fragment) {
            RouteRequest request = chain.getRequest();
            Bundle bundle = request.getExtras();
            if (bundle != null && !bundle.isEmpty()) {
                ((Fragment) targetObject).setArguments(bundle);
            }
        } else {
            RLog.i("Bundle is empty");
        }

        RouteResponse response = RouteResponse.assemble(RouteStatus.SUCCEED, null);
        if (targetObject != null) {
            response.setResult(targetObject);
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
