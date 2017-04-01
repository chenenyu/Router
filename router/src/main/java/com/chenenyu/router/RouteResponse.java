package com.chenenyu.router;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * <p>
 * Created by Cheney on 2017/3/31.
 */
public class RouteResponse implements Parcelable {
    private Intent data;
    private RouteResult result;
    private String msg;

    public RouteResponse() {
    }

    public RouteResponse(Intent data, RouteResult result, String msg) {
        this.data = data;
        this.result = result;
        this.msg = msg;
    }

    public Intent getData() {
        return data;
    }

    public void setData(Intent data) {
        this.data = data;
    }

    public RouteResult getResult() {
        return result;
    }

    public void setResult(RouteResult result) {
        this.result = result;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.data, flags);
        dest.writeInt(this.result == null ? -1 : this.result.ordinal());
        dest.writeString(this.msg);
    }

    protected RouteResponse(Parcel in) {
        this.data = in.readParcelable(Intent.class.getClassLoader());
        int tmpResult = in.readInt();
        this.result = tmpResult == -1 ? null : RouteResult.values()[tmpResult];
        this.msg = in.readString();
    }

    public static final Creator<RouteResponse> CREATOR = new Creator<RouteResponse>() {
        @Override
        public RouteResponse createFromParcel(Parcel source) {
            return new RouteResponse(source);
        }

        @Override
        public RouteResponse[] newArray(int size) {
            return new RouteResponse[size];
        }
    };
}
