package com.chenenyu.router.module

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.chenenyu.router.Router
import com.chenenyu.router.annotation.Route
import com.chenenyu.router.module1.R

@Route("module1", "router://filter/module1")
class Module1Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_module1)

        val fragment = Router.build("fragment2").getFragment(this) as Fragment
        if (fragment != null) {
            supportFragmentManager.beginTransaction().add(R.id.activity_module1, fragment).commit()
        }
    }
}
