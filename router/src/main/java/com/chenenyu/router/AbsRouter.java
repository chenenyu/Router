package com.chenenyu.router;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.support.annotation.AnimRes;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.util.SparseArray;

import com.chenenyu.router.util.RLog;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Help to construct a {@link RouteRequest}.
 * <p>
 * Created by Cheney on 2017/3/31.
 */
abstract class AbsRouter implements IRouter {
    RouteRequest mRouteRequest;

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
    @Deprecated
    public IRouter extras(Bundle bundle) {
        mRouteRequest.setExtras(bundle);
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
        } else if (value instanceof ArrayList) {
            if (!((ArrayList) value).isEmpty()) {
                Object obj = ((ArrayList) value).get(0);
                if (obj instanceof Integer) {
                    bundle.putIntegerArrayList(key, (ArrayList<Integer>) value);
                } else if (obj instanceof String) {
                    bundle.putStringArrayList(key, (ArrayList<String>) value);
                } else if (obj instanceof CharSequence) {
                    bundle.putCharSequenceArrayList(key, (ArrayList<CharSequence>) value);
                } else if (obj instanceof Parcelable) {
                    bundle.putParcelableArrayList(key, (ArrayList<? extends Parcelable>) value);
                }
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
            RLog.w("Unknown object type.");
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
    public void go(Fragment fragment, RouteCallback callback) {
        mRouteRequest.setCallback(callback);
        go(fragment);
    }

    @Override
    public void go(android.app.Fragment fragment, RouteCallback callback) {
        mRouteRequest.setCallback(callback);
        go(fragment);
    }
}
