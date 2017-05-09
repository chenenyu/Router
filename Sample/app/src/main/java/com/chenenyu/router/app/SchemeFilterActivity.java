package com.chenenyu.router.app;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.chenenyu.router.Router;

/**
 * How to handle route from browser.
 * Created by chen on 17-5-9.
 */
public class SchemeFilterActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Uri uri = getIntent().getData();
        Router.build(uri).go(this);
        finish();
    }
}
