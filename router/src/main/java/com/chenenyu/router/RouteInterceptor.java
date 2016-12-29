package com.chenenyu.router;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Interceptor before route.
 * <p>
 * Created by Cheney on 2016/12/20.
 */
public interface RouteInterceptor {
    /**
     * @param context Context
     * @param uri     Uri
     * @param extras  Bundle
     * @return True if you want to intercept this route, false otherwise.
     */
    boolean intercept(Context context, @NonNull Uri uri, @Nullable Bundle extras);
}
