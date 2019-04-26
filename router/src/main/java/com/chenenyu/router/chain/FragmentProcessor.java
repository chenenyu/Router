package com.chenenyu.router.chain;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.chenenyu.router.AptHub;
import com.chenenyu.router.MatcherRegistry;
import com.chenenyu.router.RealInterceptorChain;
import com.chenenyu.router.RouteInterceptor;
import com.chenenyu.router.RouteResponse;
import com.chenenyu.router.RouteStatus;
import com.chenenyu.router.matcher.AbsExplicitMatcher;
import com.chenenyu.router.util.RLog;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by chenenyu on 2018/6/15.
 */
public class FragmentProcessor implements RouteInterceptor {
    @NonNull
    @Override
    public RouteResponse intercept(Chain chain) {
        RealInterceptorChain realChain = (RealInterceptorChain) chain;
        Set<Map.Entry<String, Class<?>>> entries = AptHub.routeTable.entrySet();
        List<AbsExplicitMatcher> matcherList = MatcherRegistry.getExplicitMatcher();

        for (AbsExplicitMatcher matcher : matcherList) {
            for (Map.Entry<String, Class<?>> entry : entries) {
                if (matcher.match(chain.getContext(), chain.getRequest().getUri(), entry.getKey(), chain.getRequest())) {
                    RLog.i(String.format("{uri=%s, matcher=%s}",
                            chain.getRequest().getUri(), matcher.getClass().getCanonicalName()));
                    realChain.setTargetClass(entry.getValue());
                    Object result = matcher.generate(chain.getContext(), chain.getRequest().getUri(), entry.getValue());
                    if (result instanceof android.support.v4.app.Fragment) {
                        realChain.setTargetObject(result);
                    } else if (result instanceof Fragment) {
                        realChain.setTargetObject(result);
                    } else {
                        return RouteResponse.assemble(RouteStatus.FAILED, String.format(
                                "The matcher can't generate a fragment instance for uri: %s",
                                chain.getRequest().getUri().toString()));
                    }
                    return chain.process();
                }
            }
        }

        return RouteResponse.assemble(RouteStatus.NOT_FOUND, String.format(
                "Can't find a fragment that matches the given uri: %s",
                chain.getRequest().getUri().toString()));
    }
}
