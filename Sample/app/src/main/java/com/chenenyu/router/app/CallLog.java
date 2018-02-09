package com.chenenyu.router.app;

import android.util.Log;

import com.chenenyu.router.MethodCallable;
import com.chenenyu.router.annotation.Route;


/**
 * Created by chenenyu on 2018/2/5.
 */
public class CallLog implements MethodCallable {
    @Route({"router://log"})
    public static void log() {
        Log.i("CallLog", "a log shows how to call native static method from js.");
    }
}
