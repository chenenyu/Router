package com.chenenyu.router.app;

import android.app.Application;

import com.chenenyu.router.util.RLog;

/**
 * <p>
 * Created by Cheney on 2017/1/12.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        RLog.showLog(true);
    }
}
