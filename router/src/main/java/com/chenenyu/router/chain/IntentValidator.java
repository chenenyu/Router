package com.chenenyu.router.chain;

import android.support.annotation.NonNull;

import com.chenenyu.router.MatcherRegistry;
import com.chenenyu.router.RouteInterceptor;
import com.chenenyu.router.RouteResponse;
import com.chenenyu.router.RouteStatus;
import com.chenenyu.router.matcher.AbsMatcher;

import java.util.List;

/**
 * Created by chenenyu on 2018/6/15.
 */
public class IntentValidator implements RouteInterceptor {
    @NonNull
    @Override
    public RouteResponse intercept(Chain chain) {
        List<AbsMatcher> matcherList = MatcherRegistry.getMatcher();
        if (matcherList.isEmpty()) {
            return RouteResponse.assemble(RouteStatus.FAILED, "The MatcherRegistry contains no matcher.");
        }
        return chain.process();
    }
}
