package com.chenenyu.router.module

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.chenenyu.router.Router
import com.chenenyu.router.annotation.Route
import com.chenenyu.router.module1.R

@Route("module1", "router://filter/module1")
class Module1Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_module1)

        val fragment = Router.build("fragment2").getFragment(this) as Fragment
        supportFragmentManager.beginTransaction().add(R.id.activity_module1, fragment).commit()
    }
}
