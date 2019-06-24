package com.chenenyu.router.matcher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Base mather for explicit intent and fragment.
 * <p>
 * Created by chenenyu on 2017/3/12.
 */
public abstract class AbsExplicitMatcher extends AbsMatcher {

    public AbsExplicitMatcher(int priority) {
        super(priority);
    }

    @Override
    public Object generate(Context context, Uri uri, @Nullable Class<?> target) {
        if (target == null) {
            return null;
        }
        Object result = null;
        if (Activity.class.isAssignableFrom(target)) {
            result = new Intent(context, target);
        } else if (Fragment.class.isAssignableFrom(target)) {
            try {
                result = target.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

}
