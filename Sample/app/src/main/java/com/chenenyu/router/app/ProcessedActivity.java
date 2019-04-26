package com.chenenyu.router.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.TextView;

import com.chenenyu.router.annotation.Route;

@Route(value = "processedActivity", interceptors = {"CInterceptor"})
public class ProcessedActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intercepted);
        TextView textView = findViewById(R.id.textTV);

        String extraValue = getIntent().getStringExtra(CInterceptor.extraKey);
        if (TextUtils.isEmpty(extraValue)) {
            textView.setText("ohh，未能通过拦截器正确传值!!!");
        } else {
            textView.setText("通过拦截器正确传值，值为:" + extraValue);
        }
    }
}
