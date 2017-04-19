package com.chenenyu.router;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.support.annotation.AnimRes;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Size;
import android.util.SizeF;

import com.chenenyu.router.util.RLog;

import java.io.Serializable;

/**
 * <p>
 * Created by Cheney on 2017/3/31.
 */
abstract class AbsRouter implements IRouter {
    RouteRequest mRouteRequest;
    RouteResponse mRouteResponse;

    @Override
    public IRouter build(Uri uri) {
        mRouteRequest = new RouteRequest(uri);
        mRouteResponse = null; // reset
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
    @Deprecated
    public IRouter extras(Bundle bundle) {
        mRouteRequest.setExtras(bundle);
        return this;
    }

    @Override
    public IRouter with(Bundle bundle) {
        if (bundle != null && bundle.isEmpty()) {
            Bundle allBundle = mRouteRequest.getExtras();
            if (allBundle == null) {
                allBundle = new Bundle();
            }
            allBundle.putAll(bundle);
            mRouteRequest.setExtras(allBundle);
        }
        return this;
    }

    @RequiresApi(21)
    @Override
    public IRouter with(PersistableBundle bundle) {
        if (bundle != null && bundle.isEmpty()) {
            Bundle allBundle = mRouteRequest.getExtras();
            if (allBundle == null) {
                allBundle = new Bundle();
            }
            allBundle.putAll(bundle);
            mRouteRequest.setExtras(allBundle);
        }
        return this;
    }

    @Override
    public IRouter with(String key, Object value) {
        Bundle bundle = mRouteRequest.getExtras();
        if (bundle == null) {
            bundle = new Bundle();
        }
        if (value instanceof Bundle) {
            bundle.putBundle(key, (Bundle) value);
        } else if (value instanceof Byte) {
            bundle.putByte(key, (byte) value);
        } else if (value instanceof Short) {
            bundle.putShort(key, (short) value);
        } else if (value instanceof Integer) {
            bundle.putInt(key, (int) value);
        } else if (value instanceof Long) {
            bundle.putLong(key, (long) value);
        } else if (value instanceof Character) {
            bundle.putChar(key, (char) value);
        } else if (value instanceof Boolean) {
            bundle.putBoolean(key, (boolean) value);
        } else if (value instanceof Float) {
            bundle.putFloat(key, (float) value);
        } else if (value instanceof Double) {
            bundle.putDouble(key, (double) value);
        } else if (value instanceof String) {
            bundle.putString(key, (String) value);
        } else if (value instanceof CharSequence) {
            bundle.putCharSequence(key, (CharSequence) value);
        } else if (value instanceof byte[]) {
            bundle.putByteArray(key, (byte[]) value);
        } else if (value instanceof short[]) {
            bundle.putShortArray(key, (short[]) value);
        } else if (value instanceof int[]) {
            bundle.putIntArray(key, (int[]) value);
        } else if (value instanceof long[]) {
            bundle.putLongArray(key, (long[]) value);
        } else if (value instanceof char[]) {
            bundle.putCharArray(key, (char[]) value);
        } else if (value instanceof boolean[]) {
            bundle.putBooleanArray(key, (boolean[]) value);
        } else if (value instanceof float[]) {
            bundle.putFloatArray(key, (float[]) value);
        } else if (value instanceof double[]) {
            bundle.putDoubleArray(key, (double[]) value);
        } else if (value instanceof String[]) {
            bundle.putStringArray(key, (String[]) value);
        } else if (value instanceof CharSequence[]) {
            bundle.putCharSequenceArray(key, (CharSequence[]) value);
        } else if (value instanceof Parcelable[]) {
            bundle.putParcelableArray(key, (Parcelable[]) value);
        } else if (value instanceof IBinder) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                bundle.putBinder(key, (IBinder) value);
            } else {
                RLog.e("putBind() requires api 18.");
            }
        } else if (value instanceof Size) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                bundle.putSize(key, (Size) value);
            } else {
                RLog.e("'putSize' requires api 21.");
            }
        } else if (value instanceof SizeF) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                bundle.putSizeF(key, (SizeF) value);
            } else {
                RLog.e("'putSizeF' requires api 21.");
            }
        } else if (value instanceof Parcelable) {
            bundle.putParcelable(key, (Parcelable) value);
        } else if (value instanceof Serializable) {
            bundle.putSerializable(key, (Serializable) value);
        }
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
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ContextCompat.startActivity(context, intent, options);
        }

        callback(RouteResult.SUCCEED, null);
    }

    @Override
    public void go(Fragment fragment, RouteCallback callback) {
        mRouteRequest.setCallback(callback);
        go(fragment);
    }

    @Override
    public void go(Fragment fragment) {
        FragmentActivity activity = fragment.getActivity();
        Context context = fragment.getContext();
        Intent intent = getIntent(activity != null ? activity : context);
        if (intent == null) {
            return;
        }
        Bundle options = mRouteRequest.getActivityOptions() == null ?
                null : mRouteRequest.getActivityOptions().toBundle();

        if (mRouteRequest.getRequestCode() < 0) {
            fragment.startActivity(intent, options);
        } else {
            fragment.startActivityForResult(intent, mRouteRequest.getRequestCode(), options);
        }
        if (activity != null && mRouteRequest.getEnterAnim() != 0 && mRouteRequest.getExitAnim() != 0) {
            // Add transition animation.
            activity.overridePendingTransition(
                    mRouteRequest.getEnterAnim(), mRouteRequest.getExitAnim());
        }

        callback(RouteResult.SUCCEED, null);
    }

    @RequiresApi(11)
    @Override
    public void go(android.app.Fragment fragment, RouteCallback callback) {
        mRouteRequest.setCallback(callback);
        go(fragment);
    }

    @RequiresApi(11)
    @Override
    public void go(android.app.Fragment fragment) {
        Activity activity = fragment.getActivity();
        Intent intent = getIntent(activity);
        if (intent == null) {
            return;
        }
        Bundle options = mRouteRequest.getActivityOptions() == null ?
                null : mRouteRequest.getActivityOptions().toBundle();

        if (mRouteRequest.getRequestCode() < 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) { // 4.1
                fragment.startActivity(intent, options);
            } else {
                fragment.startActivity(intent);
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) { // 4.1
                fragment.startActivityForResult(intent, mRouteRequest.getRequestCode(), options);
            } else {
                fragment.startActivityForResult(intent, mRouteRequest.getRequestCode());
            }
        }
        if (activity != null && mRouteRequest.getEnterAnim() != 0 && mRouteRequest.getExitAnim() != 0) {
            // Add transition animation.
            activity.overridePendingTransition(
                    mRouteRequest.getEnterAnim(), mRouteRequest.getExitAnim());
        }

        callback(RouteResult.SUCCEED, null);
    }

    void callback(RouteResult result, String msg) {
        if (mRouteRequest.isIpc()) {  // cross process
            if (mRouteResponse != null) {
                mRouteResponse.setResult(result);
                mRouteResponse.setMsg(msg);
            }
        } else {
            if (result != RouteResult.SUCCEED) {
                RLog.w(msg);
            }
            if (mRouteRequest.getCallback() != null) {
                mRouteRequest.getCallback().callback(result, mRouteRequest.getUri(), msg);
            }
        }
    }

}
