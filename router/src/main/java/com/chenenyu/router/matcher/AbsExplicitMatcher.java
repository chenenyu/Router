package com.chenenyu.router.matcher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Base mather for explicit intent.
 * <p>
 * Created by Cheney on 2017/3/12.
 */
public abstract class AbsExplicitMatcher extends AbsMatcher {

    public AbsExplicitMatcher(int priority) {
        super(priority);
    }

    @Override
    public Intent onMatched(Context context, Uri uri, @Nullable Class<? extends Activity> target) {
        if (target == null) {
            return null;
        }
        return new Intent(context, target);
    }

}
