package com.chenenyu.router.module;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.chenenyu.router.Router;
import com.chenenyu.router.annotation.Route;
import com.chenenyu.router.module2.databinding.ActivityModule2Binding;

@Route("module2")
public class Module2Activity extends AppCompatActivity {
    private ActivityModule2Binding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityModule2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Fragment fragment = Router.build("fragment1").getFragment(this);
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().add(binding.activityModule2.getId(), fragment).commit();
        }
    }
}
