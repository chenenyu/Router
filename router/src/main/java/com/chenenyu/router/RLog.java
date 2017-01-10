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
        if (loggable) {
            Log.w(TAG, msg);
        }
    }

    public static void w(String msg, Throwable tr) {
        if (loggable) {
            Log.w(TAG, msg, tr);
        }
    }

    public static void e(String msg) {
        if (loggable) {
            Log.e(TAG, msg);
        }
    }

    public static void e(String msg, Throwable tr) {
        if (loggable) {
            Log.e(TAG, msg, tr);
        }
    }
}
