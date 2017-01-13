package com.chenenyu.router.module;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.chenenyu.router.annotation.Route;
import com.chenenyu.router.module2.R;

@Route("module2")
public class Module2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module2);
    }
}
