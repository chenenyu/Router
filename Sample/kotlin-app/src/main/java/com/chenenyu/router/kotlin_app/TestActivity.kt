package com.chenenyu.router.kotlin_app

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.chenenyu.router.annotation.Route

@Route("test")
class TestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
    }
}
