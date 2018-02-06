package com.chenenyu.router.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.chenenyu.router.Router;

public class WebActivity extends AppCompatActivity {
    WebView mWebView;
    CallToast mCallToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCallToast = new CallToast(this);
        mWebView = new WebView(this);
        mWebView.getSettings().setJavaScriptEnabled(true);
        setContentView(mWebView);
        mWebView.loadUrl("file:///android_asset/scheme.html");
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // 通过对象调用
                if (Router.build(url).go(getApplicationContext(), mCallToast)) {
                    return true;
                }
                // 通过类调用（优点是不需要实例化对象, 缺点是只能调用静态方法）
                if (Router.build(url).go(getApplicationContext(), CallLog.class)) {
                    return true;
                }

                // 页面跳转
                Router.build(url).go(getApplicationContext());
                return true;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWebView.destroy();
    }
}
