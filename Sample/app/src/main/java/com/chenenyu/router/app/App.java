package com.chenenyu.router.app;

import android.app.Application;

import com.chenenyu.router.Router;

/**
 * <p>
 * Created by Cheney on 2017/1/12.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // debug模式,显示log
        if (BuildConfig.DEBUG) {
            Router.setDebuggable(true);
        }

        // The next comment line show how to process aar module.
        // AptHub.registerModules("your-aar-module-name");

        // init
        Router.initialize(this);

        Router.addGlobalInterceptor(new GlobalInterceptor());
    }
}
