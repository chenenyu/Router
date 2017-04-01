package com.chenenyu.router.util;

import android.app.Application;

/**
 * Application instance;
 * <p>
 * Created by Cheney on 2017/3/31.
 */
public class AppUtils {
    public static Application INSTANCE;

    static {
        try {
            INSTANCE = (Application) Class.forName("android.app.AppGlobals")
                    .getMethod("getInitialApplication").invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                INSTANCE = (Application) Class.forName("android.app.ActivityThread")
                        .getMethod("currentApplication").invoke(null);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }
}
