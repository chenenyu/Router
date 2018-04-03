package com.chenenyu.router;

import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Route request object.
 * <p>
 * Created by chenenyu on 2017/3/31.
 */
@SuppressWarnings("WeakerAccess")
public class RouteRequest implements Parcelable {
    private static final int INVALID_CODE = -1;

    private Uri uri;
    private Bundle extras;
    private int flags;
    private Uri data;
    private String type;
    private String action;
    // skip all the interceptors
    private boolean skipInterceptors;
    // skip some interceptors temporarily
    @Nullable
    private HashSet<String> removedInterceptors;
    // add some interceptors temporarily
    @Nullable
    private HashSet<String> addedInterceptors;
    @Nullable
    private RouteCallback routeCallback;
    private int requestCode = INVALID_CODE;
    private int enterAnim = INVALID_CODE;
    private int exitAnim = INVALID_CODE;
    @Nullable
    private Bundle activityOptionsBundle;


    public RouteRequest(Uri uri) {
        this.uri = uri;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public Bundle getExtras() {
        return extras;
    }

    public void setExtras(Bundle extras) {
        this.extras = extras;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public void addFlags(int flags) {
        this.flags |= flags;
    }

    public Uri getData() {
        return data;
    }

    public void setData(Uri data) {
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public boolean isSkipInterceptors() {
        return skipInterceptors;
    }

    public void setSkipInterceptors(boolean skipInterceptors) {
        this.skipInterceptors = skipInterceptors;
    }

    @Nullable
    public Set<String> getAddedInterceptors() {
        return addedInterceptors;
    }

    @Nullable
    public Set<String> getRemovedInterceptors() {
        return removedInterceptors;
    }

    public void addInterceptors(String... interceptors) {
        if (interceptors == null || interceptors.length <= 0) {
            return;
        }
        if (this.addedInterceptors == null) {
            this.addedInterceptors = new HashSet<>(interceptors.length);
        }
        this.addedInterceptors.addAll(Arrays.asList(interceptors));
    }

    public void removeInterceptors(String... interceptors) {
        if (interceptors == null || interceptors.length <= 0) {
            return;
        }
        if (this.removedInterceptors == null) {
            this.removedInterceptors = new HashSet<>(interceptors.length);
        }
        this.removedInterceptors.addAll(Arrays.asList(interceptors));
    }

    @Nullable
    public RouteCallback getRouteCallback() {
        return routeCallback;
    }

    public void setRouteCallback(@Nullable RouteCallback routeCallback) {
        this.routeCallback = routeCallback;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(int requestCode) {
        if (requestCode < 0) {
            this.requestCode = INVALID_CODE;
        } else {
            this.requestCode = requestCode;
        }
    }

    public int getEnterAnim() {
        return enterAnim;
    }

    public void setEnterAnim(int enterAnim) {
        if (enterAnim < 0) {
            this.enterAnim = INVALID_CODE;
        } else {
            this.enterAnim = enterAnim;
        }
    }

    public int getExitAnim() {
        return exitAnim;
    }

    public void setExitAnim(int exitAnim) {
        if (exitAnim < 0) {
            this.exitAnim = INVALID_CODE;
        } else {
            this.exitAnim = exitAnim;
        }
    }

    @Nullable
    public Bundle getActivityOptionsBundle() {
        return activityOptionsBundle;
    }

    public void setActivityOptionsBundle(@Nullable Bundle activityOptionsBundle) {
        this.activityOptionsBundle = activityOptionsBundle;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.uri, flags);
        dest.writeBundle(this.extras);
        dest.writeInt(this.flags);
        dest.writeParcelable(this.data, flags);
        dest.writeString(this.type);
        dest.writeString(this.action);
        dest.writeByte(this.skipInterceptors ? (byte) 1 : (byte) 0);
        dest.writeSerializable(this.removedInterceptors);
        dest.writeSerializable(this.addedInterceptors);
        dest.writeSerializable(this.routeCallback);
        dest.writeInt(this.requestCode);
        dest.writeInt(this.enterAnim);
        dest.writeInt(this.exitAnim);
        dest.writeBundle(this.activityOptionsBundle);
    }

    @SuppressWarnings("unchecked")
    protected RouteRequest(Parcel in) {
        this.uri = in.readParcelable(Uri.class.getClassLoader());
        this.extras = in.readBundle(Bundle.class.getClassLoader());
        this.flags = in.readInt();
        this.data = in.readParcelable(Uri.class.getClassLoader());
        this.type = in.readString();
        this.action = in.readString();
        this.skipInterceptors = in.readByte() != 0;
        this.removedInterceptors = (HashSet<String>) in.readSerializable();
        this.addedInterceptors = (HashSet<String>) in.readSerializable();
        this.routeCallback = (RouteCallback) in.readSerializable();
        this.requestCode = in.readInt();
        this.enterAnim = in.readInt();
        this.exitAnim = in.readInt();
        this.activityOptionsBundle = in.readBundle(Bundle.class.getClassLoader());
    }

    public static final Creator<RouteRequest> CREATOR = new Creator<RouteRequest>() {
        @Override
        public RouteRequest createFromParcel(Parcel source) {
            return new RouteRequest(source);
        }

        @Override
        public RouteRequest[] newArray(int size) {
            return new RouteRequest[size];
        }
    };
}
