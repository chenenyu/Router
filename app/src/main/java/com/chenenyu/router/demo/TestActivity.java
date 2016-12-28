package com.chenenyu.router.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.chenenyu.router.annotation.Route;

@Route("test")
public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
    }
}
