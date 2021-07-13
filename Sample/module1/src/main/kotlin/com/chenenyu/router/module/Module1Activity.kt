package com.chenenyu.router.module

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.chenenyu.router.Router
import com.chenenyu.router.annotation.Route
import com.chenenyu.router.module1.databinding.ActivityModule1Binding

@Route("module1", "router://filter/module1")
class Module1Activity : AppCompatActivity() {

    private lateinit var binding: ActivityModule1Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModule1Binding.inflate(layoutInflater)
        setContentView(binding.root)
        val fragment = Router.build("fragment2").getFragment(this) as Fragment
        supportFragmentManager.beginTransaction().add(binding.activityModule1.id, fragment).commit()
    }
}
