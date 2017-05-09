package com.chenenyu.router.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.chenenyu.router.annotation.Route;

@Route({"test", "http://example.com/user", "router://test"})
public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        TextView text = (TextView) findViewById(R.id.text_test);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && !bundle.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("id:")
                    .append(bundle.getString("id"))
                    .append("\n")
                    .append("status:")
                    .append(bundle.getString("status"));
            text.setText(sb.toString());
        }
    }
}
