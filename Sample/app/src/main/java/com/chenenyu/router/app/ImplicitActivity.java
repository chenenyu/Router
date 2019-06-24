package com.chenenyu.router.app;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ImplicitActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheme);

        TextView text = findViewById(R.id.text_test);
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
