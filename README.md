![Build Status](https://travis-ci.org/chenenyu/Router.svg?branch=master) ![license](https://img.shields.io/badge/license-Apache%202-yellow.svg) ![PullRequest](https://img.shields.io/badge/PRs-welcome-brightgreen.svg) 

# Router

建议浏览[中文wiki](https://github.com/chenenyu/Router/wiki). It's better than you think.

![screenshot](static/screenshot.gif)

## Getting started

*  Add dependencies by adding the following lines to your `build.gradle`:  

```Groovy
android {
    defaultConfig {
        ...
        javaCompileOptions {
            annotationProcessorOptions {
                arguments = ["moduleName": project.name]
            }
        }
    }
}

dependencies {
    implementation 'com.chenenyu.router:router:版本号'
    // 每个使用了@Router注解的module都要添加该注解处理器
    annotationProcessor 'com.chenenyu.router:compiler:版本号'
}
```

latest `router` version: ![Download](https://api.bintray.com/packages/chenenyu/maven/router/images/download.svg)

latest `compiler` version: ![compiler](https://api.bintray.com/packages/chenenyu/maven/router-compiler/images/download.svg)  

## 基本用法

1. 初始化

```java
Router.initialize(new Configuration.Builder()
        // 调试模式，开启后会打印log
        .setDebuggable(BuildConfig.DEBUG)
        // 模块名(即project.name)，每个使用Router的module都要在这里注册
        .registerModules("your app module", "your lib module", "other module")
        .build());
```


2. 添加拦截器(可选)

```java
@Interceptor("SampleInterceptor")
public class SampleInterceptor implements RouteInterceptor {
    @Override
    public boolean intercept(Context context, RouteRequest routeRequest) {
        // do something
        return false;
    }
}
```

3. 添加注解

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

4. 跳转

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
        public void callback(RouteResult state, Uri uri, String message) {
             // do something
        }
});
// 获取路由对应的intent
Router.build("test").getIntent();
// 获取注解的Fragment
Router.build("test").getFragment();
```

## 进阶用法

Please refer to the [wiki](https://github.com/chenenyu/Router/wiki) for more information.

## ProGuard

```
# Router
-keep class com.chenenyu.router.** {*;}
-keep class * implements com.chenenyu.router.template.ParamInjector {*;}
```

## 讨论

QQ group: 271849001

## Donate ❤️

[Click here](https://github.com/chenenyu/Router/wiki/Donate).

## License

[Apache 2.0](https://github.com/chenenyu/Router/blob/master/LICENSE)