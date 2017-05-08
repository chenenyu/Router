package com.chenenyu.router.module;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.chenenyu.router.Router;
import com.chenenyu.router.annotation.Route;
import com.chenenyu.router.module1.R;

@Route("module1")
public class Module1Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module1);

        Fragment fragment = (Fragment) Router.build("fragment2").getFragment(this);
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().add(R.id.activity_module1, fragment).commit();
        }
    }

}
