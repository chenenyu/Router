package com.chenenyu.router.app;

import android.app.Application;

import com.chenenyu.router.AptHub;
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

        // The next line shows how to process modules (e.g. aar modules).
        AptHub.registerModules("module1", "module2");

        // init
        Router.initialize(this);

//        Router.addGlobalInterceptor(new GlobalInterceptor());
    }
}
