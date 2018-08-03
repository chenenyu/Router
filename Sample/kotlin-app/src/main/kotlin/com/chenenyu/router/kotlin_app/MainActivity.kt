package com.chenenyu.router.kotlin_app

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.chenenyu.router.Router
import com.chenenyu.router.util.RLog
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        RLog.showLog(true)
        btn.setOnClickListener { Router.build("test").go(this) }
    }
}
