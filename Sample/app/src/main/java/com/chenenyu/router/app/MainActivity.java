package com.chenenyu.router.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.chenenyu.router.RouteCallback;
import com.chenenyu.router.RouteStatus;
import com.chenenyu.router.Router;
import com.chenenyu.router.app.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ActivityMainBinding binding;
    private String uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.editRoute.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                uri = s.toString();
                binding.btn0.setText(getString(R.string.go_to, s));
            }
        });

        Router.addGlobalInterceptor(new GlobalInterceptor());

        // 动态添加路由
        Router.handleRouteTable(map -> map.put("dynamic", DynamicActivity.class));

//        Router.handleInterceptorTable(new InterceptorTable() {
//            @Override
//            public void handle(Map<String, Class<? extends RouteInterceptor>> map) {
//                Log.d("InterceptorTable", map.toString());
//            }
//        });

//        Router.handleTargetInterceptors(new TargetInterceptors() {
//            @Override
//            public void handle(Map<Class<?>, String[]> map) {
//                Log.d("TargetInterceptors", map.toString());
//            }
//        });
    }

    @Override
    public void onClick(View v) {
        if (v == binding.btn0) {
            // 添加结果回调
            Router.build(uri).callback((RouteCallback) (status, uri, message) -> {
                if (status == RouteStatus.SUCCEED) {
                    Toast.makeText(MainActivity.this, "succeed: " + uri.toString(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "error: " + uri + ", " + message, Toast.LENGTH_SHORT).show();
                }
            }).go(this);
        } else if (v == binding.btn1) {
            Router.build(binding.btn1.getText().toString()).go(this);
        } else if (v == binding.btn2) {
            Router.build("dynamic").go(this);
        } else if (v == binding.btn3) {
//            Bundle bundle = new Bundle();
//            bundle.putString("extra", "Bundle from MainActivity.");
//            Router.build("result").requestCode(0).with(bundle).go(this);
            Router.build("result").requestCode(0).with("extra", "Bundle from MainActivity.").go(this);
        } else if (v == binding.btn4) {
            startActivity(new Intent(this, WebActivity.class));
        } else if (v == binding.btn5) {
            Router.build(Uri.parse("router://implicit?id=9527&status=success")).go(this);
        } else if (v == binding.btn6) {
            Router.build(binding.btn6.getText().toString()).go(this);
        } else if (v == binding.btn7) {
            Router.build("module1").go(this);
        } else if (v == binding.btn8) {
            Router.build("module2").go(this);
        } else if (v == binding.btn9) {
            Router.build("intercepted").go(this);
        } else if (v == binding.btn10) {
            Router.build("intercepted").skipInterceptors().go(this);
        } else if (v == binding.btn11) {
            Router.build("test").addInterceptors("AInterceptor").go(this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            String result = data.getStringExtra("extra");
            Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
        }
    }
}
