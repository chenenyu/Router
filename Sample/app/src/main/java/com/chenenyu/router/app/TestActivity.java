package com.chenenyu.router.app;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.chenenyu.router.Router;
import com.chenenyu.router.annotation.InjectParam;
import com.chenenyu.router.annotation.Route;
import com.chenenyu.router.app.databinding.ActivityTestBinding;

@Route({"test", "http://example.com/user", "router://filter/test"})
public class TestActivity extends AppCompatActivity {
    @InjectParam
    String id = "0000";
    @InjectParam(key = "status")
    String sts = "default";

    @InjectParam
    short test1;
    @InjectParam
    byte[] test2;
    @InjectParam
    Model test3;
    @InjectParam
    Model test4;

    private ActivityTestBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Router.injectParams(this);

        Bundle mExtras = getIntent().getExtras();
        id = mExtras.getString("id", id);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null && !bundle.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("id:")
                    .append(id)
                    .append("\n")
                    .append("status:")
                    .append(sts);
            binding.textTest.setText(sb.toString());
        }
    }
}
