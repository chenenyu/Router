package com.chenenyu.router.module;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.chenenyu.router.RouteCallback;
import com.chenenyu.router.RouteResult;
import com.chenenyu.router.Router;
import com.chenenyu.router.annotation.Route;
import com.chenenyu.router.module1.R;

@Route("module1")
public class Module1Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module1);
        Button btn = (Button) findViewById(R.id.btn_go);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Router.build("module2").callback(new RouteCallback() {
                    @Override
                    public void callback(RouteResult state, Uri uri, String message) {
                        Toast.makeText(Module1Activity.this, state.name() + ", " + uri, Toast.LENGTH_SHORT).show();
                    }
                }).go(Module1Activity.this);
                finish();
            }
        });
    }

}
