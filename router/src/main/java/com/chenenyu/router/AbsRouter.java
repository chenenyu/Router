package com.chenenyu.router;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.AnimRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;

import com.chenenyu.router.util.RLog;

/**
 * <p>
 * Created by Cheney on 2017/3/31.
 */
public abstract class AbsRouter implements IRouter {
    protected RouteRequest mRouteRequest;

    @Override
    public IRouter build(Uri uri) {
        mRouteRequest = new RouteRequest(uri);
        return this;
    }

    @Override
    public IRouter callback(RouteCallback callback) {
        mRouteRequest.setCallback(callback);
        return this;
    }

    @Override
    public IRouter requestCode(int requestCode) {
        mRouteRequest.setRequestCode(requestCode);
        return this;
    }

    @Override
    public IRouter extras(Bundle bundle) {
        mRouteRequest.setExtras(bundle);
        return this;
    }

    @Override
    public IRouter addFlags(int flags) {
        mRouteRequest.addFlags(flags);
        return this;
    }

    @Override
    public IRouter anim(@AnimRes int enterAnim, @AnimRes int exitAnim) {
        mRouteRequest.setEnterAnim(enterAnim);
        mRouteRequest.setExitAnim(exitAnim);
        return this;
    }

    @Override
    public IRouter activityOptions(ActivityOptionsCompat activityOptions) {
        mRouteRequest.setActivityOptions(activityOptions);
        return this;
    }

    @Override
    public IRouter skipInterceptors() {
        mRouteRequest.setSkipInterceptors(true);
        return this;
    }

    @Override
    public void go(Context context, RouteCallback callback) {
        mRouteRequest.setCallback(callback);
        go(context);
    }

    @Override
    public void go(Context context) {
        Intent intent = getIntent(context);
        if (intent == null) {
            return;
        }
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        Bundle options = mRouteRequest.getActivityOptions() == null ?
                null : mRouteRequest.getActivityOptions().toBundle();

        if (context instanceof Activity) {
            ActivityCompat.startActivityForResult((Activity) context, intent,
                    mRouteRequest.getRequestCode(), options);

            if (mRouteRequest.getEnterAnim() != 0 && mRouteRequest.getExitAnim() != 0) {
                // Add transition animation.
                ((Activity) context).overridePendingTransition(
                        mRouteRequest.getEnterAnim(), mRouteRequest.getExitAnim());
            }
        } else {
            RLog.w("Please pass an Activity context to call method 'startActivityForResult'");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ActivityCompat.startActivity(context, intent, options);
        }
    }

}
