package com.chenenyu.router;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by chenenyu on 2018/6/15.
 */
public final class RouteResponse {

    public static final int STATUS_PROCESSING = 1;
    public static final int STATUS_INTERCEPTED = 1 << 1;
    public static final int STATUS_SUCCEED = 1 << 2;
    public static final int STATUS_FAILED = 1 << 3;

    private RouteStatus status = RouteStatus.PROCESSING;
    private String msg; // assemble msg
    @Nullable
    private Object result;

    private RouteResponse() {
    }

    public static RouteResponse assemble(@NonNull RouteStatus status, @Nullable String msg) {
        RouteResponse response = new RouteResponse();
        response.status = status;
        response.msg = msg;
        return response;
    }

    public boolean isSuccessful() {
        return status == RouteStatus.SUCCEED;
    }
//
//    public boolean isProcessing() {
//        return status == RouteStatus.PROCESSING;
//    }

    public RouteStatus getStatus() {
        return status;
    }

    public void setStatus(RouteStatus status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    @Nullable
    public Object getResult() {
        return result;
    }

    public void setResult(@Nullable Object result) {
        this.result = result;
    }
}
