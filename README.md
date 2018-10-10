![Build Status](https://travis-ci.org/chenenyu/Router.svg?branch=master) ![license](https://img.shields.io/badge/license-Apache%202-yellow.svg) ![PullRequest](https://img.shields.io/badge/PRs-welcome-brightgreen.svg) 

# Router

[中文wiki](https://github.com/chenenyu/Router/wiki). 方便的话给个star!❤️

![screenshot](static/screenshot.gif)

## Getting started

#### [Branch 1.5 see here](https://github.com/chenenyu/Router/tree/1.5)

*  Add router gradle plugin to your project-level `build.gradle`, as shown below.

```Groovy
buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:+'
        classpath "com.chenenyu.router:gradle-plugin:版本号"
    }
}
```
latest `router-gradle-plugin` version: ![Download](https://api.bintray.com/packages/chenenyu/maven/router-gradle-plugin/images/download.svg)


* Apply router plugin in your module-level 'build.gradle'.

```Groovy
apply plugin: 'com.android.application' // apply plugin: 'com.android.library'
apply plugin: 'com.chenenyu.router'
```

**注意**: 在rootProject的`build.gradle`文件中, 可以指定插件引用的library版本.

```groovy
ext {
    routerVersion = 'x.y.z'
    compilerVersion = 'x.y.z'
}
```
latest `router` version: ![Download](https://api.bintray.com/packages/chenenyu/maven/router/images/download.svg)

latest `compiler` version: ![compiler](https://api.bintray.com/packages/chenenyu/maven/router-compiler/images/download.svg)


## 基本用法

* 添加拦截器(可选)

```java
@Interceptor("SampleInterceptor")
public class SampleInterceptor implements RouteInterceptor {
    @Override
    public RouteResponse intercept(Chain chain) {
        // do something
        return chain.process();
    }
}
```

* 添加注解

```java
// 给Activity添加注解，指定了路径和拦截器(可选)
@Route(value = "test", interceptors = "SampleInterceptor")
public class TestActivity extends AppCompatActivity {
    @InjectParam(key="foo") // 参数映射
    String foo;
  
     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Router.injectParams(this);  // 自动从bundle中获取并注入参数
        ...
    }
}

// 给Fragment添加注解
@Route("test")
public class TestFragment extends Fragment {
    ...
}
```

* 跳转

```java
// 简单跳转
Router.build("test").go(this);
// startActivityForResult
Router.build("test").requestCode(0).go(this);
// 携带bundle参数
Router.build("test").with("key", Object).go(this);
// 添加回调
Router.build("test").go(this, new RouteCallback() {
    @Override
    public void callback(RouteStatus status, Uri uri, String message) {
        // do something
    }
});

// 获取路由对应的intent
Router.build("test").getIntent();
// 获取注解的Fragment
Router.build("test").getFragment();
```

## 进阶用法

建议浏览 [wiki](https://github.com/chenenyu/Router/wiki).


## 谁在使用Router

<div>
  <a href="http://sj.qq.com/myapp/detail.htm?apkName=com.sankuai.erp.mcashier">
  	<img src="static/美团轻收银.png" width="100"/>
  </a>
  <a href="http://www.sixiangyun.cn/">
    <img src="static/私享云.png" width="100"/>
  </a>
  <a href="#">
  	<img src="static/恒大智能家居.png" width="100"/>
  </a>
  <a href="https://fir.im/shouba">
  	<img src="static/批车吧.png" width="100"/>
  </a>
  <a href="https://fir.im/ebeilun">
  	<img src="static/e乡北仑.png" width="100"/>
  </a>
  <a href="https://fir.im/duihuan">
  	<img src="static/硬币自循环.png" width="100"/>
  </a>
</div>

## 讨论

QQ group: 271849001

## Donate ❤️

[Click here](https://github.com/chenenyu/Router/wiki/Donate).

## License

[Apache 2.0](https://github.com/chenenyu/Router/blob/master/LICENSE)