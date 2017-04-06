package com.chenenyu.router.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.chenenyu.router.annotation.Route;

@Route("result")
public class ForResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_for_result);

        String extra = getIntent().getStringExtra("extra");
        TextView textExtra = (TextView) findViewById(R.id.text_extra);
        textExtra.setText(extra);

        Button result = (Button) findViewById(R.id.btn_result);
        result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK, new Intent().putExtra("extra", "Result from ForResultActivity"));
                finish();
            }
        });
    }
}
