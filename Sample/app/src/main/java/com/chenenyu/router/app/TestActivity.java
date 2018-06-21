package com.chenenyu.router.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.chenenyu.router.Router;
import com.chenenyu.router.annotation.InjectParam;
import com.chenenyu.router.annotation.Route;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        Router.injectParams(this);

        Bundle mExtras = getIntent().getExtras();
        id = mExtras.getString("id", id);

        TextView text = findViewById(R.id.text_test);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && !bundle.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("id:")
                    .append(id)
                    .append("\n")
                    .append("status:")
                    .append(sts);
            text.setText(sb.toString());
        }
    }
}
