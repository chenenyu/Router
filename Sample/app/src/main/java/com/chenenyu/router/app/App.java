package com.chenenyu.router.app;

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
        // debug模式,显示log
//        Router.setDebuggable(true);
        Router.initialize(this, true);
    }
}
