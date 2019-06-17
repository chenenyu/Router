package com.chenenyu.router.matcher;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.Nullable;

/**
 * Base mather for implicit intent.
 * <p>
 * Created by chenenyu on 2017/3/12.
 */
public abstract class AbsImplicitMatcher extends AbsMatcher {

    public AbsImplicitMatcher(int priority) {
        super(priority);
    }

    @Override
    public Object generate(Context context, Uri uri, @Nullable Class<?> target) {
        return new Intent(Intent.ACTION_VIEW, uri);
    }

}
