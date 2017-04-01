package com.chenenyu.router.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.chenenyu.router.annotation.Route;

@Route(value = "intercepted", interceptors = "SampleInterceptor")
public class InterceptedActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intercepted);
    }
}
