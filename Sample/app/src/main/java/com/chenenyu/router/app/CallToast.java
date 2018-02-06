package com.chenenyu.router.app;

import android.content.Context;
import android.widget.Toast;

import com.chenenyu.router.MethodCallable;
import com.chenenyu.router.annotation.InjectParam;
import com.chenenyu.router.annotation.Route;


/**
 * 使用了{@link Route}注解的方法所在的类必须实现{@link MethodCallable}接口, 才能被Router识别并调用
 * <br>
 * Created by chenenyu on 2018/2/5.
 */
public class CallToast implements MethodCallable {
    private Context mContext;

    public CallToast(Context context) {
        mContext = context;
    }

    @Route("router://toast1")
    public void toast() {
        Toast.makeText(mContext, "a toast shows how to call native method from js.", Toast.LENGTH_SHORT).show();
    }

    @Route("router://toast2")
    public void toastWithParams(@InjectParam(key = "param") String msg) {
        Toast.makeText(mContext, "msg from js: " + msg, Toast.LENGTH_SHORT).show();
    }

}
