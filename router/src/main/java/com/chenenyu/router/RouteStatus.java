package com.chenenyu.router;

/**
 * Result for each route.
 * <p>
 * Created by chenenyu on 2017/3/9.
 */
public enum RouteStatus {
    PROCESSING,
    SUCCEED,
    INTERCEPTED,
    NOT_FOUND,
    FAILED;

    public boolean isSuccessful() {
        return this.equals(SUCCEED);
    }
}
