package com.chenenyu.router;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.ArrayMap;
import android.util.Pair;

import com.chenenyu.router.annotation.InjectParam;
import com.chenenyu.router.annotation.Route;
import com.chenenyu.router.matcher.AbsExplicitMatcher;
import com.chenenyu.router.matcher.AbsImplicitMatcher;
import com.chenenyu.router.matcher.AbsMatcher;
import com.chenenyu.router.util.RLog;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Core of router.
 * <br>
 * Created by chenenyu on 2017/3/30.
 */
class RealRouter extends AbsRouter {
    private static RealRouter sInstance;
    private static final String PARAM_CLASS_SUFFIX = "$$Router$$ParamInjector";

    private Map<String, RouteInterceptor> mInterceptorInstance = new HashMap<>();

    private RealRouter() {
    }

    static synchronized RealRouter getInstance() {
        if (sInstance == null) {
            sInstance = new RealRouter();
        }
        return sInstance;
    }

    /**
     * Handle route table.
     *
     * @param routeTable route table
     */
    void handleRouteTable(RouteTable routeTable) {
        if (routeTable != null) {
            routeTable.handle(AptHub.routeTable);
        }
    }

    /**
     * Handle interceptor table.
     *
     * @param interceptorTable interceptor table
     */
    void handleInterceptorTable(InterceptorTable interceptorTable) {
        if (interceptorTable != null) {
            interceptorTable.handle(AptHub.interceptorTable);
        }
    }

    /**
     * Handle targets' interceptors.
     *
     * @param targetInterceptors target -> interceptors
     */
    void handleTargetInterceptors(TargetInterceptors targetInterceptors) {
        if (targetInterceptors != null) {
            targetInterceptors.handle(AptHub.targetInterceptors);
        }
    }

    /**
     * Auto inject params from bundle.
     *
     * @param obj Activity or Fragment.
     */
    void injectParams(Object obj) {
        if (obj instanceof Activity || obj instanceof Fragment || obj instanceof android.app.Fragment) {
            String key = obj.getClass().getCanonicalName();
            Class<ParamInjector> clz;
            if (!AptHub.injectors.containsKey(key)) {
                try {
                    //noinspection unchecked
                    clz = (Class<ParamInjector>) Class.forName(key + PARAM_CLASS_SUFFIX);
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

    private void callback(RouteResult result, String msg) {
        if (result != RouteResult.SUCCEED) {
            RLog.w(msg);
        }
        if (mRouteRequest.getCallback() != null) {
            mRouteRequest.getCallback().callback(result, mRouteRequest.getUri(), msg);
        }
    }

    @Override
    public Object getFragment(Context context) {
        if (mRouteRequest.getUri() == null) {
            callback(RouteResult.FAILED, "uri == null.");
            return null;
        }

        if (!mRouteRequest.isSkipInterceptors()) {
            for (RouteInterceptor interceptor : Router.getGlobalInterceptors()) {
                if (interceptor.intercept(context, mRouteRequest)) {
                    callback(RouteResult.INTERCEPTED, String.format(
                            "Intercepted by global interceptor: %s.",
                            interceptor.getClass().getSimpleName()));
                    return null;
                }
            }
        }

        // Fragment只能匹配显式Matcher
        List<AbsExplicitMatcher> matcherList = MatcherRegistry.getExplicitMatcher();
        if (matcherList.isEmpty()) {
            callback(RouteResult.FAILED, "The MatcherRegistry contains no AbsExplicitMatcher.");
            return null;
        }

        // fragment only matches explicit route
        if (AptHub.routeTable.isEmpty()) {
            callback(RouteResult.FAILED, "The route table contains no mapping.");
            return null;
        }

        Set<Map.Entry<String, Class<?>>> entries = AptHub.routeTable.entrySet();

        for (AbsExplicitMatcher matcher : matcherList) {
            for (Map.Entry<String, Class<?>> entry : entries) {
                if (matcher.match(context, mRouteRequest.getUri(), entry.getKey(), mRouteRequest)) {
                    RLog.i("Caught by " + matcher.getClass().getCanonicalName());
                    if (intercept(context, assembleClassInterceptors(entry.getValue()))) {
                        return null;
                    }
                    Object result = matcher.generate(context, mRouteRequest.getUri(), entry.getValue());
                    if (result instanceof Fragment) {
                        Fragment fragment = (Fragment) result;
                        Bundle bundle = mRouteRequest.getExtras();
                        if (bundle != null && !bundle.isEmpty()) {
                            fragment.setArguments(bundle);
                        }
                        return fragment;
                    } else if (result instanceof android.app.Fragment) {
                        android.app.Fragment fragment = (android.app.Fragment) result;
                        Bundle bundle = mRouteRequest.getExtras();
                        if (bundle != null && !bundle.isEmpty()) {
                            fragment.setArguments(bundle);
                        }
                        return fragment;
                    } else {
                        callback(RouteResult.FAILED, String.format(
                                "The matcher can't generate a fragment instance for uri: %s",
                                mRouteRequest.getUri().toString()));
                        return null;
                    }
                }
            }
        }

        callback(RouteResult.FAILED, String.format(
                "Can not find an Fragment that matches the given uri: %s", mRouteRequest.getUri()));
        return null;
    }

    @Override
    public Intent getIntent(Context context) {
        if (mRouteRequest.getUri() == null) {
            callback(RouteResult.FAILED, "uri == null.");
            return null;
        }

        if (!mRouteRequest.isSkipInterceptors()) {
            for (RouteInterceptor interceptor : Router.getGlobalInterceptors()) {
                if (interceptor.intercept(context, mRouteRequest)) {
                    callback(RouteResult.INTERCEPTED, String.format(
                            "Intercepted by global interceptor: %s.",
                            interceptor.getClass().getSimpleName()));
                    return null;
                }
            }
        }

        List<AbsMatcher> matcherList = MatcherRegistry.getMatcher();
        if (matcherList.isEmpty()) {
            callback(RouteResult.FAILED, "The MatcherRegistry contains no Matcher.");
            return null;
        }

        Set<Map.Entry<String, Class<?>>> entries = AptHub.routeTable.entrySet();

        for (AbsMatcher matcher : matcherList) {
            if (AptHub.routeTable.isEmpty()) { // implicit totally.
                if (matcher.match(context, mRouteRequest.getUri(), null, mRouteRequest)) {
                    RLog.i("Caught by " + matcher.getClass().getCanonicalName());
                    return finalizeIntent(context, matcher, null);
                }
            } else {
                boolean isiImplicit = matcher instanceof AbsImplicitMatcher;
                for (Map.Entry<String, Class<?>> entry : entries) {
                    if (matcher.match(context, mRouteRequest.getUri(), isiImplicit ? null : entry.getKey(), mRouteRequest)) {
                        RLog.i("Caught by " + matcher.getClass().getCanonicalName());
                        return finalizeIntent(context, matcher, isiImplicit ? null : entry.getValue());
                    }
                }
            }
        }

        callback(RouteResult.FAILED, String.format(
                "Can not find an Activity that matches the given uri: %s", mRouteRequest.getUri()));
        return null;
    }

    /**
     * Do intercept and then generate intent by the given matcher, finally assemble extras.
     *
     * @param context Context
     * @param matcher current matcher
     * @param target  route target
     * @return Finally intent.
     */
    private Intent finalizeIntent(Context context, AbsMatcher matcher, @Nullable Class<?> target) {
        if (intercept(context, assembleClassInterceptors(target))) {
            return null;
        }
        Object result = matcher.generate(context, mRouteRequest.getUri(), target);
        if (result instanceof Intent) {
            Intent intent = (Intent) result;
            if (mRouteRequest.getExtras() != null && !mRouteRequest.getExtras().isEmpty()) {
                intent.putExtras(mRouteRequest.getExtras());
            }
            if (mRouteRequest.getFlags() != 0) {
                intent.addFlags(mRouteRequest.getFlags());
            }
            if (mRouteRequest.getData() != null) {
                intent.setData(mRouteRequest.getData());
            }
            if (mRouteRequest.getType() != null) {
                intent.setType(mRouteRequest.getType());
            }
            if (mRouteRequest.getAction() != null) {
                intent.setAction(mRouteRequest.getAction());
            }
            return intent;
        } else {
            callback(RouteResult.FAILED, String.format(
                    "The matcher can't generate an intent for uri: %s",
                    mRouteRequest.getUri().toString()));
            return null;
        }
    }

    /**
     * Assemble final interceptors for class.
     *
     * @param target activity or fragment
     * @return Interceptors set.
     */
    private Set<String> assembleClassInterceptors(@Nullable Class<?> target) {
        // Assemble final interceptors
        Set<String> finalInterceptors = new HashSet<>();
        if (target != null) {
            // 1. Add original interceptors in Map
            String[] baseInterceptors = AptHub.targetInterceptors.get(target);
            if (baseInterceptors != null && baseInterceptors.length > 0) {
                Collections.addAll(finalInterceptors, baseInterceptors);
            }
            // 2. Skip temp removed interceptors
            if (mRouteRequest.getRemovedInterceptors() != null) {
                finalInterceptors.removeAll(mRouteRequest.getRemovedInterceptors());
            }
        }
        // 3. Add temp added interceptors
        if (mRouteRequest.getAddedInterceptors() != null) {
            finalInterceptors.addAll(mRouteRequest.getAddedInterceptors());
        }
        return finalInterceptors;
    }

    /**
     * 从method列表中找出相应的方法
     *
     * @return Method
     */
    private Method getMethod(Context context, ArrayMap<Method, Pair<String[], String[]>> map) {
        if (mRouteRequest.getUri() == null) {
            callback(RouteResult.FAILED, "uri == null.");
            return null;
        }

        if (!mRouteRequest.isSkipInterceptors()) {
            for (RouteInterceptor interceptor : Router.getGlobalInterceptors()) {
                if (interceptor.intercept(context, mRouteRequest)) {
                    callback(RouteResult.INTERCEPTED, String.format(
                            "Intercepted by global interceptor: %s.",
                            interceptor.getClass().getSimpleName()));
                    return null;
                }
            }
        }

        // Method只匹配显式Matcher
        List<AbsExplicitMatcher> matcherList = MatcherRegistry.getExplicitMatcher();
        if (matcherList.isEmpty()) {
            callback(RouteResult.FAILED, "The MatcherRegistry contains no AbsExplicitMatcher.");
            return null;
        }

        Set<Map.Entry<Method, Pair<String[], String[]>>> entries = map.entrySet();
        for (AbsExplicitMatcher matcher : matcherList) {
            for (Map.Entry<Method, Pair<String[], String[]>> entry : entries) {
                for (String route : entry.getValue().first) {
                    if (matcher.match(context, mRouteRequest.getUri(), route, mRouteRequest)) {
                        if (!mRouteRequest.isSkipInterceptors() &&
                                intercept(context, assembleMethodInterceptors(entry.getValue().second))) {
                            return null;
                        }
                        return entry.getKey();
                    }
                }
            }
        }

        callback(RouteResult.FAILED, String.format(
                "Can not find an method that matches the given uri: %s", mRouteRequest.getUri()));
        return null;
    }

    /**
     * Fetch method's args.
     *
     * @param method target method.
     * @return args array.
     */
    @Nullable
    private Object[] getMethodArgs(Method method) {
        // 获取参数类型
        Class[] paramTypes = method.getParameterTypes();
        // 获取参数注解
        Annotation[][] paramAnnotations = method.getParameterAnnotations();
        if (paramTypes != null && paramTypes.length > 0) { // 有参
            if (paramTypes.length != paramAnnotations.length) {
                callback(RouteResult.FAILED,
                        String.format("Each parameter of method[%s] must be annotated by @InjectParam.",
                                method.getName()));
                return null;
            }
            Object[] args = new Object[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++) {
                Class paramType = paramTypes[i];
                if (paramType != String.class) {
                    callback(RouteResult.FAILED,
                            String.format("Method[%s] has a non-string type parameter: %s",
                                    method.getName(), paramType.getSimpleName()));
                    return null;
                }
                Annotation annotation = paramAnnotations[i][0];
                if (annotation instanceof InjectParam) {
                    InjectParam injectParam = (InjectParam) annotation;
                    args[i] = mRouteRequest.getExtras().getString(injectParam.key());
                } else {
                    callback(RouteResult.FAILED,
                            String.format("The parameter of method[%s] is annotated by @%s, however it must be @InjectParam.",
                                    method.getName(), annotation.annotationType().getSimpleName()));
                    return null;
                }
            }
            return args;
        }

        callback(RouteResult.FAILED, String.format("Method[%s] has no parameters", method.getName()));
        return null;
    }

    /**
     * Assemble final interceptors for method.
     *
     * @param baseInterceptors interceptors defined in method annotation
     * @return Interceptors set.
     */
    private Set<String> assembleMethodInterceptors(String[] baseInterceptors) {
        // Assemble final interceptors
        Set<String> finalInterceptors = new HashSet<>();
        // 1. Add original interceptors in array
        if (baseInterceptors != null && baseInterceptors.length > 0) {
            Collections.addAll(finalInterceptors, baseInterceptors);
        }
        // 2. Skip temp removed interceptors
        if (mRouteRequest.getRemovedInterceptors() != null) {
            finalInterceptors.removeAll(mRouteRequest.getRemovedInterceptors());
        }
        // 3. Add temp added interceptors
        if (mRouteRequest.getAddedInterceptors() != null) {
            finalInterceptors.addAll(mRouteRequest.getAddedInterceptors());
        }
        return finalInterceptors;
    }

    /**
     * Find interceptors
     *
     * @param context           Context
     * @param finalInterceptors all interceptors
     * @return True if intercepted, false otherwise.
     */
    private boolean intercept(Context context, Set<String> finalInterceptors) {
        if (mRouteRequest.isSkipInterceptors()) {
            return false;
        }

        if (finalInterceptors != null && !finalInterceptors.isEmpty()) {
            for (String name : finalInterceptors) {
                RouteInterceptor interceptor = mInterceptorInstance.get(name);
                if (interceptor == null) {
                    Class<? extends RouteInterceptor> clz = AptHub.interceptorTable.get(name);
                    try {
                        Constructor<? extends RouteInterceptor> constructor = clz.getConstructor();
                        interceptor = constructor.newInstance();
                        mInterceptorInstance.put(name, interceptor);
                    } catch (Exception e) {
                        RLog.e("Can't construct a interceptor with name: " + name);
                        e.printStackTrace();
                    }
                }
                // do intercept
                if (interceptor != null && interceptor.intercept(context, mRouteRequest)) {
                    callback(RouteResult.INTERCEPTED, String.format(
                            "Intercepted: {uri: %s, interceptor: %s}",
                            mRouteRequest.getUri().toString(), name));
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void go(Context context) {
        Intent intent = getIntent(context);
        if (intent == null) {
            return;
        }

        Bundle options = mRouteRequest.getActivityOptionsCompat() == null ?
                null : mRouteRequest.getActivityOptionsCompat().toBundle();

        if (context instanceof Activity) {
            ActivityCompat.startActivityForResult((Activity) context, intent,
                    mRouteRequest.getRequestCode(), options);

            if (mRouteRequest.getEnterAnim() >= 0 && mRouteRequest.getExitAnim() >= 0) {
                // Add transition animation.
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

        callback(RouteResult.SUCCEED, null);
    }

    @Override
    public void go(Fragment fragment) {
        FragmentActivity activity = fragment.getActivity();
        Context context = fragment.getContext();
        Intent intent = getIntent(activity != null ? activity : context);
        if (intent == null) {
            return;
        }
        Bundle options = mRouteRequest.getActivityOptionsCompat() == null ?
                null : mRouteRequest.getActivityOptionsCompat().toBundle();

        if (mRouteRequest.getRequestCode() < 0) {
            fragment.startActivity(intent, options);
        } else {
            fragment.startActivityForResult(intent, mRouteRequest.getRequestCode(), options);
        }
        if (activity != null && mRouteRequest.getEnterAnim() >= 0 && mRouteRequest.getExitAnim() >= 0) {
            // Add transition animation.
            activity.overridePendingTransition(
                    mRouteRequest.getEnterAnim(), mRouteRequest.getExitAnim());
        }

        callback(RouteResult.SUCCEED, null);
    }

    @Override
    public void go(android.app.Fragment fragment) {
        Activity activity = fragment.getActivity();
        Intent intent = getIntent(activity);
        if (intent == null) {
            return;
        }
        Bundle options = mRouteRequest.getActivityOptionsCompat() == null ?
                null : mRouteRequest.getActivityOptionsCompat().toBundle();

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
        if (activity != null && mRouteRequest.getEnterAnim() >= 0 && mRouteRequest.getExitAnim() >= 0) {
            // Add transition animation.
            activity.overridePendingTransition(
                    mRouteRequest.getEnterAnim(), mRouteRequest.getExitAnim());
        }

        callback(RouteResult.SUCCEED, null);
    }

    @Override
    public boolean go(Context context, MethodCallable callable) {
        Class clz = callable.getClass();
        ArrayMap<Method, Pair<String[], String[]>> map = new ArrayMap<>();
        for (Method method : clz.getDeclaredMethods()) {
            Route route = method.getAnnotation(Route.class);
            if (route != null) {
                map.put(method, new Pair<>(route.value(), route.interceptors()));
            }
        }
        if (map.isEmpty()) {
            callback(RouteResult.FAILED, String.format("%s contains no method that annotated by Route.",
                    clz.getSimpleName()));
            return false;
        } else {
            Method method = getMethod(context, map);
            if (method == null) {
                return false;
            }
            try {
                method.setAccessible(true);
                // 获取参数类型
                Class[] paramTypes = method.getParameterTypes();
                if (paramTypes != null && paramTypes.length > 0) { // 有参
                    Object[] args = getMethodArgs(method);
                    if (args == null) {
                        return false;
                    }

                    if (Modifier.isStatic(method.getModifiers())) { // static method
                        method.invoke(null, args);
                    } else {
                        method.invoke(callable, args);
                    }
                } else { // 无参
                    if (Modifier.isStatic(method.getModifiers())) { // static method
                        method.invoke(null);
                    } else {
                        method.invoke(callable);
                    }
                }
            } catch (Exception e) {
                callback(RouteResult.FAILED, e.getMessage());
                return false;
            }
        }

        callback(RouteResult.SUCCEED, null);
        return true;
    }

    @Override
    public boolean go(Context context, Class<? extends MethodCallable> clz) {
        ArrayMap<Method, Pair<String[], String[]>> map = new ArrayMap<>();
        for (Method method : clz.getDeclaredMethods()) {
            Route route = method.getAnnotation(Route.class);
            if (route != null && Modifier.isStatic(method.getModifiers())) {
                map.put(method, new Pair<>(route.value(), route.interceptors()));
            }
        }
        if (map.isEmpty()) {
            callback(RouteResult.FAILED, String.format("%s contains no method that annotated by Route.",
                    clz.getSimpleName()));
            return false;
        } else {
            Method method = getMethod(context, map);
            if (method == null) {
                return false;
            }
            if (!Modifier.isStatic(method.getModifiers())) {
                callback(RouteResult.FAILED, String.format("Method[%s] must be static", method.getName()));
            }
            try {
                method.setAccessible(true);
                // 获取参数类型
                Class[] paramTypes = method.getParameterTypes();
                if (paramTypes != null && paramTypes.length > 0) { // 有参
                    Object[] args = getMethodArgs(method);
                    if (args == null) {
                        return false;
                    }
                    method.invoke(null, args);
                } else { // 无参
                    method.invoke(null);
                }
            } catch (Exception e) {
                callback(RouteResult.FAILED, e.getMessage());
                return false;
            }
        }

        callback(RouteResult.SUCCEED, null);
        return true;
    }

}
