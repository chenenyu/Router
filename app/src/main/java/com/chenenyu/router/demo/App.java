package com.chenenyu.router.demo;

import android.support.multidex.MultiDexApplication;

import com.chenenyu.router.Router;

/**
 * <p>
 * Created by Cheney on 2017/1/12.
 */
public class App extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        Router.initialize(this);
        // 开启log
        if (BuildConfig.DEBUG) {
            Router.openLog();
        }
    }
}
