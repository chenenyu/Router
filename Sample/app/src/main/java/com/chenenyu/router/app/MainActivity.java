package com.chenenyu.router.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.chenenyu.router.RouteCallback;
import com.chenenyu.router.RouteResult;
import com.chenenyu.router.RouteTable;
import com.chenenyu.router.Router;

import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private String uri;
    private Button btn0, btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText editRoute = (EditText) findViewById(R.id.edit_route);
        btn0 = (Button) findViewById(R.id.btn0);
        btn1 = (Button) findViewById(R.id.btn1);
        btn2 = (Button) findViewById(R.id.btn2);
        btn3 = (Button) findViewById(R.id.btn3);
        btn4 = (Button) findViewById(R.id.btn4);
        btn5 = (Button) findViewById(R.id.btn5);
        btn6 = (Button) findViewById(R.id.btn6);
        btn7 = (Button) findViewById(R.id.btn7);
        btn8 = (Button) findViewById(R.id.btn8);
        btn9 = (Button) findViewById(R.id.btn9);

        editRoute.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                uri = s.toString();
                btn0.setText(getString(R.string.go_to, s));
            }
        });

        // 动态添加路由
        Router.addRouteTable(new RouteTable() {
            @Override
            public void handle(Map<String, Class<?>> map) {
                map.put("dynamic", DynamicActivity.class);
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v == btn0) {
            Router.build(uri).callback(new RouteCallback() { // 添加结果回调
                @Override
                public void callback(RouteResult state, Uri uri, String message) {
                    if (state == RouteResult.SUCCEED) {
                        Toast.makeText(MainActivity.this, "succeed: " + uri.toString(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "error: " + uri + ", " + message, Toast.LENGTH_SHORT).show();
                    }
                }
            }).go(this);
        } else if (v == btn1) {
            Router.build(btn1.getText().toString()).go(this);
        } else if (v == btn2) {
            Router.build("dynamic").go(this);
        } else if (v == btn3) {
//            Bundle bundle = new Bundle();
//            bundle.putString("extra", "Bundle from MainActivity.");
//            Router.build("result").requestCode(0).with(bundle).go(this);
            Router.build("result").requestCode(0).with("extra", "Bundle from MainActivity.").go(this);
        } else if (v == btn4) {
            startActivity(new Intent(this, WebActivity.class));
        } else if (v == btn5) {
            Router.build(Uri.parse("router://implicit?id=9527&status=success")).go(this);
        } else if (v == btn6) {
            Router.build(btn6.getText().toString()).go(this);
        } else if (v == btn7) {
            Router.build("module1").go(this);
        } else if (v == btn8) {
            Router.build("module2").go(this);
        } else if (v == btn9) {
            Router.build("intercepted").go(this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == RESULT_OK) {
            String result = data.getStringExtra("extra");
            Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
        }
    }
}
