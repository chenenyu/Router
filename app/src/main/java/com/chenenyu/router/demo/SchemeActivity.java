package com.chenenyu.router.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class SchemeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheme);


        TextView text = (TextView) findViewById(R.id.text_test);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && !bundle.isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("id:")
                    .append(bundle.getString("id"))
                    .append("\n")
                    .append("key:")
                    .append(bundle.getString("key"));
            text.setText(stringBuilder.toString());
        }
    }
}
