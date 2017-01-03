package com.chenenyu.router;

import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * Options during a route.
 * <p>
 * Created by Cheney on 2017/1/3.
 */
public class RouteOptions {
    private int flags;
    private int requestCode = -1;
    @Nullable
    private RouteCallback callback;
    @Nullable
    private Bundle bundle;
    private int enterAnim;
    private int exitAnim;

    public void addFlags(int flags) {
        this.flags |= flags;
    }

    public int getFlags() {
        return flags;
    }

    public void setRequestCode(int requestCode) {
        this.requestCode = requestCode;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public void setCallback(@Nullable RouteCallback callback) {
        this.callback = callback;
    }

    @Nullable
    public RouteCallback getCallback() {
        return callback;
    }

    public void setBundle(@Nullable Bundle bundle) {
        this.bundle = bundle;
    }

    @Nullable
    public Bundle getBundle() {
        return bundle;
    }

    public void setAnim(int enterAnim, int exitAnim) {
        this.enterAnim = enterAnim;
        this.exitAnim = exitAnim;
    }

    public int getEnterAnim() {
        return enterAnim;
    }

    public int getExitAnim() {
        return exitAnim;
    }

    /**
     * Reset fields.
     */
    public void reset() {
        flags = 0;
        requestCode = -1;
        callback = null;
        bundle = null;
        enterAnim = 0;
        exitAnim = 0;
    }
}
