package com.chenenyu.router;

import android.util.Log;

/**
 * Internal simple log.
 * <p>
 * Created by Cheney on 2016/12/27.
 */
class RLog {
    private static final String TAG = "Router";
    private static boolean loggable = false;

    protected static void openLog() {
        loggable = true;
    }

    public static void v(String tag, String msg) {
        if (loggable) {
            Log.v(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (loggable) {
            Log.d(tag, msg);
        }
    }

    public static void i(String msg) {
        if (loggable) {
            Log.i(TAG, msg);
        }
    }

    public static void i(String msg, Throwable tr) {
        if (loggable) {
            Log.i(TAG, msg, tr);
        }
    }

    public static void w(String msg) {
        w(TAG, msg);
    }

    public static void w(String tag, String msg) {
        if (loggable) {
            Log.w(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (loggable) {
            Log.e(tag, msg);
        }
    }

}
