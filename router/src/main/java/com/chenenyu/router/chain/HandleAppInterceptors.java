package com.chenenyu.router.chain;

import android.support.annotation.NonNull;

import com.chenenyu.router.AptHub;
import com.chenenyu.router.RealInterceptorChain;
import com.chenenyu.router.RouteInterceptor;
import com.chenenyu.router.RouteRequest;
import com.chenenyu.router.RouteResponse;
import com.chenenyu.router.Router;
import com.chenenyu.router.util.RLog;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Collect app interceptors and insert into chain queue to process.
 * <br>
 * Created by chenenyu on 2018/6/15.
 */
public class HandleAppInterceptors implements RouteInterceptor {
    @NonNull
    @Override
    public RouteResponse intercept(Chain chain) {
        if (chain.getRequest().isSkipInterceptors()) {
            return chain.process();
        }

        RealInterceptorChain realChain = (RealInterceptorChain) chain;
        RouteRequest request = chain.getRequest();

        // enqueue global interceptors
        if (!Router.getGlobalInterceptors().isEmpty()) {
            realChain.getInterceptors().addAll(Router.getGlobalInterceptors());
        }

        Set<String> finalInterceptors = new LinkedHashSet<>();
        // add annotated interceptors
        if (realChain.getTargetClass() != null) {
            String[] baseInterceptors = AptHub.targetInterceptorsTable.get(realChain.getTargetClass());
            if (baseInterceptors != null && baseInterceptors.length > 0) {
                Collections.addAll(finalInterceptors, baseInterceptors);
            }
        }

        // add/remove temp interceptors
        if (request.getTempInterceptors() != null) {
            for (Map.Entry<String, Boolean> entry : request.getTempInterceptors().entrySet()) {
                if (entry.getValue() == Boolean.TRUE) {
                    finalInterceptors.add(entry.getKey());
                } else {
                    finalInterceptors.remove(entry.getKey());
                }
            }
        }

        if (!finalInterceptors.isEmpty()) {
            for (String name : finalInterceptors) {
                RouteInterceptor interceptor = AptHub.interceptorInstances.get(name);
                if (interceptor == null) {
                    Class<? extends RouteInterceptor> clz = AptHub.interceptorTable.get(name);
                    try {
                        interceptor = clz.newInstance();
                        AptHub.interceptorInstances.put(name, interceptor);
                    } catch (Exception e) {
                        RLog.e("Can't construct a interceptor instance for: " + name, e);
                    }
                }
                // enqueue
                if (interceptor != null) {
                    realChain.getInterceptors().add(interceptor);
                }
            }
        }

        return chain.process();
    }
}
