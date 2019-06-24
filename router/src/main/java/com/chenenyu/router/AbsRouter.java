package com.chenenyu.router;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.util.SparseArray;

import androidx.annotation.AnimRes;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.chenenyu.router.util.RLog;

import java.io.Serializable;

/**
 * Help to construct a {@link RouteRequest}.
 * <p>
 * Created by chenenyu on 2017/3/31.
 */
abstract class AbsRouter implements IRouter {
    RouteRequest mRouteRequest;

    @Override
    public IRouter build(Uri uri) {
        mRouteRequest = new RouteRequest(uri);
        Bundle bundle = new Bundle();
        bundle.putString(Router.RAW_URI, uri == null ? null : uri.toString());
        mRouteRequest.setExtras(bundle);
        return this;
    }

    @Override
    public IRouter build(@NonNull RouteRequest request) {
        mRouteRequest = request;
        Bundle bundle = mRouteRequest.getExtras();
        if (bundle == null) {
            bundle = new Bundle();
        }
        bundle.putString(Router.RAW_URI, request.getUri().toString());
        mRouteRequest.setExtras(bundle);
        return this;
    }

    @Override
    public IRouter callback(RouteCallback callback) {
        mRouteRequest.setRouteCallback(callback);
        return this;
    }

    @Override
    public IRouter requestCode(int requestCode) {
        mRouteRequest.setRequestCode(requestCode);
        return this;
    }

    @Override
    public IRouter with(Bundle bundle) {
        if (bundle != null && !bundle.isEmpty()) {
            Bundle extras = mRouteRequest.getExtras();
            if (extras == null) {
                extras = new Bundle();
            }
            extras.putAll(bundle);
            mRouteRequest.setExtras(extras);
        }
        return this;
    }

    @RequiresApi(21)
    @Override
    public IRouter with(PersistableBundle bundle) {
        if (bundle != null && !bundle.isEmpty()) {
            Bundle extras = mRouteRequest.getExtras();
            if (extras == null) {
                extras = new Bundle();
            }
            extras.putAll(bundle);
            mRouteRequest.setExtras(extras);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public IRouter with(String key, Object value) {
        if (value == null) {
            RLog.w("Ignored: The extra value is null.");
            return this;
        }
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
        } else if (value instanceof IBinder) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                bundle.putBinder(key, (IBinder) value);
            } else {
                RLog.e("putBinder() requires api 18.");
            }
        } else if (value instanceof SparseArray) {
            bundle.putSparseParcelableArray(key, (SparseArray<? extends Parcelable>) value);
        } else if (value instanceof Parcelable) {
            bundle.putParcelable(key, (Parcelable) value);
        } else if (value instanceof Parcelable[]) {
            bundle.putParcelableArray(key, (Parcelable[]) value);
        } else if (value instanceof Serializable) {
            bundle.putSerializable(key, (Serializable) value);
        } else {
            RLog.w("Unknown object type: " + value.getClass().getName());
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
    public IRouter setData(Uri data) {
        mRouteRequest.setData(data);
        return this;
    }

    @Override
    public IRouter setType(String type) {
        mRouteRequest.setType(type);
        return this;
    }

    @Override
    public IRouter setDataAndType(Uri data, String type) {
        mRouteRequest.setData(data);
        mRouteRequest.setType(type);
        return this;
    }

    @Override
    public IRouter setAction(String action) {
        mRouteRequest.setAction(action);
        return this;
    }

    @Override
    public IRouter anim(@AnimRes int enterAnim, @AnimRes int exitAnim) {
        mRouteRequest.setEnterAnim(enterAnim);
        mRouteRequest.setExitAnim(exitAnim);
        return this;
    }

    @Override
    public IRouter activityOptionsBundle(Bundle activityOptionsBundle) {
        mRouteRequest.setActivityOptionsBundle(activityOptionsBundle);
        return this;
    }

    @Override
    public IRouter skipInterceptors() {
        mRouteRequest.setSkipInterceptors(true);
        return this;
    }

    @Override
    public IRouter skipInterceptors(String... interceptors) {
        mRouteRequest.removeInterceptors(interceptors);
        return this;
    }

    @Override
    public IRouter addInterceptors(String... interceptors) {
        mRouteRequest.addInterceptors(interceptors);
        return this;
    }

    @Override
    public void go(Context context, RouteCallback callback) {
        mRouteRequest.setRouteCallback(callback);
        go(context);
    }

    @Override
    public void go(Fragment fragment, RouteCallback callback) {
        mRouteRequest.setRouteCallback(callback);
        go(fragment);
    }
}
