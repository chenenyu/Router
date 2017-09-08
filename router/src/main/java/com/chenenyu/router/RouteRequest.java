package com.chenenyu.router;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.ArraySet;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Set;

/**
 * Route request object.
 * <p>
 * Created by Cheney on 2017/3/31.
 */
public class RouteRequest implements Serializable {
    private static final int INVALID_REQUEST_CODE = -1;

    private Uri uri;
    private Bundle extras;
    private int flags;
    // skip all the interceptors
    private boolean skipInterceptors;
    // skip some interceptors temporarily
    @Nullable
    private Set<String> removedInterceptors;
    // add some interceptors temporarily
    @Nullable
    private Set<String> addedInterceptors;
    @Nullable
    private RouteCallback callback;
    private int requestCode = INVALID_REQUEST_CODE;
    private int enterAnim;
    private int exitAnim;
    @Nullable
    private ActivityOptionsCompat activityOptionsCompat;


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

    public boolean isSkipInterceptors() {
        return skipInterceptors;
    }

    public void setSkipInterceptors(boolean skipInterceptors) {
        this.skipInterceptors = skipInterceptors;
    }

    @Nullable
    public Set<String> getAddedInterceptors() {
        return addedInterceptors;
    }

    @Nullable
    public Set<String> getRemovedInterceptors() {
        return removedInterceptors;
    }

    public void addInterceptors(String... interceptors) {
        if (interceptors == null || interceptors.length <= 0) {
            return;
        }
        if (this.addedInterceptors == null) {
            this.addedInterceptors = new ArraySet<>(interceptors.length);
        }
        this.addedInterceptors.addAll(Arrays.asList(interceptors));
    }

    public void removeInterceptors(String... interceptors) {
        if (interceptors == null || interceptors.length <= 0) {
            return;
        }
        if (this.removedInterceptors == null) {
            this.removedInterceptors = new ArraySet<>(interceptors.length);
        }
        this.removedInterceptors.addAll(Arrays.asList(interceptors));
    }

    @Nullable
    public RouteCallback getCallback() {
        return callback;
    }

    public void setCallback(@Nullable RouteCallback callback) {
        this.callback = callback;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(int requestCode) {
        if (requestCode < 0) {
            this.requestCode = INVALID_REQUEST_CODE;
        } else {
            this.requestCode = requestCode;
        }
    }

    public int getEnterAnim() {
        return enterAnim;
    }

    public void setEnterAnim(int enterAnim) {
        this.enterAnim = enterAnim;
    }

    public int getExitAnim() {
        return exitAnim;
    }

    public void setExitAnim(int exitAnim) {
        this.exitAnim = exitAnim;
    }

    public ActivityOptionsCompat getActivityOptionsCompat() {
        return activityOptionsCompat;
    }

    public void setActivityOptionsCompat(ActivityOptionsCompat activityOptionsCompat) {
        this.activityOptionsCompat = activityOptionsCompat;
    }
}
