package com.chenenyu.router.chain;

import android.content.Intent;

import androidx.annotation.NonNull;

import com.chenenyu.router.AptHub;
import com.chenenyu.router.MatcherRegistry;
import com.chenenyu.router.RealInterceptorChain;
import com.chenenyu.router.RouteInterceptor;
import com.chenenyu.router.RouteRequest;
import com.chenenyu.router.RouteResponse;
import com.chenenyu.router.RouteStatus;
import com.chenenyu.router.matcher.AbsExplicitMatcher;
import com.chenenyu.router.matcher.AbsImplicitMatcher;
import com.chenenyu.router.matcher.AbsMatcher;
import com.chenenyu.router.util.RLog;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by chenenyu on 2018/6/15.
 */
public class IntentProcessor implements RouteInterceptor {
    @NonNull
    @Override
    public RouteResponse intercept(Chain chain) {
        RealInterceptorChain realChain = (RealInterceptorChain) chain;
        RouteRequest request = chain.getRequest();

        Intent intent = null;
        if (AptHub.routeTable.isEmpty()) {
            List<AbsImplicitMatcher> implicitMatcherList = MatcherRegistry.getImplicitMatcher();

            for (AbsImplicitMatcher implicitMatcher : implicitMatcherList) {
                if (implicitMatcher.match(chain.getContext(), request.getUri(), null, request)) {
                    RLog.i(String.format("{uri=%s, matcher=%s}",
                            chain.getRequest().getUri(), implicitMatcher.getClass().getCanonicalName()));
                    realChain.setTargetClass(null);
                    Object result = implicitMatcher.generate(chain.getContext(), request.getUri(), null);
                    if (result instanceof Intent) {
                        intent = (Intent) result;
                        realChain.setTargetInstance(intent);
                    } else {
                        return RouteResponse.assemble(RouteStatus.FAILED, String.format(
                                "The matcher can't generate an intent for uri: %s",
                                request.getUri().toString()));
                    }
                    break;
                }
            }
        } else {
            List<AbsMatcher> matcherList = MatcherRegistry.getMatcher();
            List<AbsExplicitMatcher> explicitMatcherList = MatcherRegistry.getExplicitMatcher();
            Set<Map.Entry<String, Class<?>>> entries = AptHub.routeTable.entrySet();

            MATCHER:
            for (AbsMatcher matcher : request.isSkipImplicitMatcher() ? explicitMatcherList : matcherList) {
                boolean isImplicit = matcher instanceof AbsImplicitMatcher;
                if (isImplicit) {
                    if (matcher.match(chain.getContext(), request.getUri(), null, request)) {
                        RLog.i(String.format("{uri=%s, matcher=%s}",
                                chain.getRequest().getUri(), matcher.getClass().getCanonicalName()));
                        realChain.setTargetClass(null);
                        Object result = matcher.generate(chain.getContext(), request.getUri(), null);
                        if (result instanceof Intent) {
                            intent = (Intent) result;
                            realChain.setTargetInstance(intent);
                        } else {
                            return RouteResponse.assemble(RouteStatus.FAILED, String.format(
                                    "The matcher can't generate an intent for uri: %s",
                                    request.getUri().toString()));
                        }
                        break;
                    }
                } else {
                    for (Map.Entry<String, Class<?>> entry : entries) {
                        if (matcher.match(chain.getContext(), request.getUri(), entry.getKey(), request)) {
                            RLog.i(String.format("{uri=%s, matcher=%s}",
                                    chain.getRequest().getUri(), matcher.getClass().getCanonicalName()));
                            realChain.setTargetClass(entry.getValue());
                            Object result = matcher.generate(chain.getContext(), request.getUri(), entry.getValue());
                            if (result instanceof Intent) {
                                intent = (Intent) result;
                                realChain.setTargetInstance(intent);
                            } else {
                                return RouteResponse.assemble(RouteStatus.FAILED, String.format(
                                        "The matcher can't generate an intent for uri: %s",
                                        request.getUri().toString()));
                            }
                            break MATCHER;
                        }
                    }
                }

            }
        }

        if (intent == null) {
            return RouteResponse.assemble(RouteStatus.NOT_FOUND, String.format(
                    "Can't find an activity that matches the given uri: %s",
                    request.getUri().toString()));
        }
        return chain.process();
    }
}
