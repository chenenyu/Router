package com.chenenyu.router;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Route request object.
 * <p>
 * Created by chenenyu on 2017/3/31.
 */
public final class RouteRequest {
    private static final int INVALID_CODE = -1;

    private Uri uri;
    private Bundle extras;
    private int flags;
    private Uri data;
    private String type;
    private String action;
    // skip all the interceptors
    private boolean skipInterceptors;
    @Nullable
    private Map<String, Boolean> tempInterceptors;
    @Nullable
    private RouteCallback routeCallback;
    private int requestCode = INVALID_CODE;
    private int enterAnim = INVALID_CODE;
    private int exitAnim = INVALID_CODE;
    @Nullable
    private Bundle activityOptionsBundle;


    public RouteRequest(Uri uri) {
        this.uri = uri;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public Bundle getExtras() {
        return extras;
    }

    public void setExtras(Bundle extras) {
        this.extras = extras;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public void addFlags(int flags) {
        this.flags |= flags;
    }

    public Uri getData() {
        return data;
    }

    public void setData(Uri data) {
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public boolean isSkipInterceptors() {
        return skipInterceptors;
    }

    public void setSkipInterceptors(boolean skipInterceptors) {
        this.skipInterceptors = skipInterceptors;
    }

    @Nullable
    public Map<String, Boolean> getTempInterceptors() {
        return tempInterceptors;
    }

    public void addInterceptors(String... interceptors) {
        if (interceptors == null || interceptors.length <= 0) {
            return;
        }
        if (this.tempInterceptors == null) {
            this.tempInterceptors = new LinkedHashMap<>(interceptors.length);
        }
        for (String interceptor : interceptors) {
            this.tempInterceptors.put(interceptor, Boolean.TRUE);
        }
    }

    public void removeInterceptors(String... interceptors) {
        if (interceptors == null || interceptors.length <= 0) {
            return;
        }
        if (this.tempInterceptors == null) {
            this.tempInterceptors = new LinkedHashMap<>(interceptors.length);
        }
        for (String interceptor : interceptors) {
            this.tempInterceptors.put(interceptor, Boolean.FALSE);
        }
    }

    @Nullable
    public RouteCallback getRouteCallback() {
        return routeCallback;
    }

    public void setRouteCallback(@Nullable RouteCallback routeCallback) {
        this.routeCallback = routeCallback;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(int requestCode) {
        if (requestCode < 0) {
            this.requestCode = INVALID_CODE;
        } else {
            this.requestCode = requestCode;
        }
    }

    public int getEnterAnim() {
        return enterAnim;
    }

    public void setEnterAnim(int enterAnim) {
        if (enterAnim < 0) {
            this.enterAnim = INVALID_CODE;
        } else {
            this.enterAnim = enterAnim;
        }
    }

    public int getExitAnim() {
        return exitAnim;
    }

    public void setExitAnim(int exitAnim) {
        if (exitAnim < 0) {
            this.exitAnim = INVALID_CODE;
        } else {
            this.exitAnim = exitAnim;
        }
    }

    @Nullable
    public Bundle getActivityOptionsBundle() {
        return activityOptionsBundle;
    }

    public void setActivityOptionsBundle(@Nullable Bundle activityOptionsBundle) {
        this.activityOptionsBundle = activityOptionsBundle;
    }

}
