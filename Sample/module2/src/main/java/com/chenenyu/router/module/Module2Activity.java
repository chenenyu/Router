package com.chenenyu.router.module;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.chenenyu.router.Router;
import com.chenenyu.router.annotation.Route;
import com.chenenyu.router.module2.R;

@Route("module2")
public class Module2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module2);

        Fragment fragment = Router.build("fragment1").getFragment(this);
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().add(R.id.activity_module2, fragment).commit();
        }
    }
}
