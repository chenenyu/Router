package com.chenenyu.router.app;

import android.app.Application;

import com.chenenyu.router.Configuration;
import com.chenenyu.router.Router;

/**
 * <p>
 * Created by Cheney on 2017/1/12.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // init
        Router.initialize(new Configuration.Builder()
                .setDebuggable(BuildConfig.DEBUG)
                .registerModules("module1", "module2", "app")
                .build());

//        Router.addGlobalInterceptor(new GlobalInterceptor());
    }
}
