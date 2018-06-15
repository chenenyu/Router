package com.chenenyu.router;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

import com.chenenyu.router.chain.BaseValidator;
import com.chenenyu.router.chain.CollectAppInterceptors;
import com.chenenyu.router.chain.FragmentProcessor;
import com.chenenyu.router.chain.FragmentValidator;
import com.chenenyu.router.chain.IntentProcessor;
import com.chenenyu.router.chain.IntentValidator;
import com.chenenyu.router.template.ParamInjector;
import com.chenenyu.router.util.RLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Core of router.
 * <br>
 * Created by chenenyu on 2017/3/30.
 */
class RealRouter extends AbsRouter {
    private List<RouteInterceptor> mIntentInterceptors = new ArrayList<>();
    private List<RouteInterceptor> mFragmentInterceptors = new ArrayList<>();

    private static final ThreadLocal<RealRouter> mRouterThreadLocal = new ThreadLocal<RealRouter>() {
        @Override
        protected RealRouter initialValue() {
            return new RealRouter();
        }
    };

    private RealRouter() {
        mIntentInterceptors.add(new BaseValidator());
        mIntentInterceptors.add(new IntentValidator());
        mIntentInterceptors.add(new IntentProcessor());
        mIntentInterceptors.add(new CollectAppInterceptors());
        mFragmentInterceptors.add(new BaseValidator());
        mFragmentInterceptors.add(new FragmentValidator());
        mFragmentInterceptors.add(new FragmentProcessor());
        mFragmentInterceptors.add(new CollectAppInterceptors());
    }

    static RealRouter getInstance() {
        return mRouterThreadLocal.get();
    }

    /**
     * Auto inject params from bundle.
     *
     * @param obj Activity or Fragment.
     */
    static void injectParams(Object obj) {
        if (obj instanceof Activity || obj instanceof Fragment || obj instanceof android.app.Fragment) {
            String key = obj.getClass().getCanonicalName();
            Class<ParamInjector> clz;
            if (!AptHub.injectors.containsKey(key)) {
                try {
                    //noinspection unchecked
                    clz = (Class<ParamInjector>) Class.forName(key + AptHub.PARAM_CLASS_SUFFIX);
                    AptHub.injectors.put(key, clz);
                } catch (ClassNotFoundException e) {
                    RLog.e("Inject params failed.", e);
                    return;
                }
            } else {
                clz = AptHub.injectors.get(key);
            }
            try {
                ParamInjector injector = clz.newInstance();
                injector.inject(obj);
            } catch (Exception e) {
                RLog.e("Inject params failed.", e);
            }
        } else {
            RLog.e("The obj you passed must be an instance of Activity or Fragment.");
        }
    }

    private void callback(RouteStatus result, String msg) {
        if (result != RouteStatus.SUCCEED) {
            RLog.w(msg);
        }
        if (mRouteRequest.getRouteCallback() != null) {
            mRouteRequest.getRouteCallback().callback(result, mRouteRequest.getUri(), msg);
        }
    }

    @Override
    public Object getFragment(Object source) {
        RealInterceptorChain chain = new RealInterceptorChain(source, mRouteRequest, mFragmentInterceptors);
        RouteResponse response = chain.process();
        callback(response.getStatus(), response.getMsg());
        return response.getResult();
    }

    @Override
    public Intent getIntent(Object source) {
        RealInterceptorChain chain = new RealInterceptorChain(source, mRouteRequest, mIntentInterceptors);
        RouteResponse response = chain.process();
        callback(response.getStatus(), response.getMsg());
        return (Intent) response.getResult();
    }

    @Override
    public void go(Context context) {
        Intent intent = getIntent(context);
        if (intent != null) {
            Bundle options = mRouteRequest.getActivityOptionsBundle();
            if (context instanceof Activity) {
                ActivityCompat.startActivityForResult((Activity) context, intent,
                        mRouteRequest.getRequestCode(), options);
                if (mRouteRequest.getEnterAnim() >= 0 && mRouteRequest.getExitAnim() >= 0) {
                    ((Activity) context).overridePendingTransition(
                            mRouteRequest.getEnterAnim(), mRouteRequest.getExitAnim());
                }
            } else {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                // The below api added in v4:25.1.0
                // ContextCompat.startActivity(context, intent, options);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    context.startActivity(intent, options);
                } else {
                    context.startActivity(intent);
                }
            }
        }
    }

    @Override
    public void go(Fragment fragment) {
        Intent intent = getIntent(fragment);
        if (intent != null) {
            Bundle options = mRouteRequest.getActivityOptionsBundle();
            if (mRouteRequest.getRequestCode() < 0) {
                fragment.startActivity(intent, options);
            } else {
                fragment.startActivityForResult(intent, mRouteRequest.getRequestCode(), options);
            }
            if (mRouteRequest.getEnterAnim() >= 0 && mRouteRequest.getExitAnim() >= 0 && fragment.getActivity() != null) {
                // Add transition animation.
                fragment.getActivity().overridePendingTransition(
                        mRouteRequest.getEnterAnim(), mRouteRequest.getExitAnim());
            }
        }
    }

    @Override
    public void go(android.app.Fragment fragment) {
        Intent intent = getIntent(fragment);
        if (intent != null) {
            Bundle options = mRouteRequest.getActivityOptionsBundle();
            if (mRouteRequest.getRequestCode() < 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) { // 4.1
                    fragment.startActivity(intent, options);
                } else {
                    fragment.startActivity(intent);
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) { // 4.1
                    fragment.startActivityForResult(intent, mRouteRequest.getRequestCode(), options);
                } else {
                    fragment.startActivityForResult(intent, mRouteRequest.getRequestCode());
                }
            }
            if (mRouteRequest.getEnterAnim() >= 0 && mRouteRequest.getExitAnim() >= 0 && fragment.getActivity() != null) {
                // Add transition animation.
                fragment.getActivity().overridePendingTransition(
                        mRouteRequest.getEnterAnim(), mRouteRequest.getExitAnim());
            }
        }
    }
}
