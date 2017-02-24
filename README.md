[![Build Status](https://travis-ci.org/chenenyu/Router.svg?branch=master)](https://travis-ci.org/chenenyu/Router) ![license](https://img.shields.io/badge/license-Apache%202-yellow.svg) [![version](https://img.shields.io/github/release/chenenyu/Router.svg)]  (https://github.com/chenenyu/Router/releases) ![API](https://img.shields.io/badge/API-9%2B-orange.svg) ![PullRequest](https://img.shields.io/badge/PRs-welcome-brightgreen.svg) 

# Router

> A *component* based router library, featuring simple and flexible.

建议浏览[中文wiki](https://github.com/chenenyu/Router/wiki).

![screenshot](static/screenshot.gif)

## Getting started

You should use a version of the Android gradle plugin 2.2 or above to supoort annotation processor.

*  Add dependencies by adding the following lines to your top level `project/build.gradle`:  

```Groovy
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:2.2.x ↑'
        classpath 'com.chenenyu.router:gradle-plugin:latest.integration'
    }
}

// Optional. Specify the dependencies version, default to the latest version.
ext {
    ...
	routerVersion = "x.y.z"
	compilerVersion = "x.y.z"
}
```

* Apply router plugin in your `app/build.gradle` or `lib/build.gradle`:  

```  Groovy
apply plugin: 'com.android.application/library'
apply plugin: 'com.chenenyu.router'
```  

current `router-gradle-plugin` version: ![Download](https://api.bintray.com/packages/chenenyu/maven/router-gradle-plugin/images/download.svg)

current `router` version: ![Download](https://api.bintray.com/packages/chenenyu/maven/router/images/download.svg)

current `router-compiler` version: ![compiler](https://api.bintray.com/packages/chenenyu/maven/router-compiler/images/download.svg)  

|router-gradle-plugin version|router version|router-compiler version|
|:---:|:---:|:---:|
|0.1.0|0.4.0|0.2.0|
|0.2.0|0.5.0|0.2.0|
|0.3.0|0.6.0|0.3.0|

## Simple usage

`Router` uses annotation to specify the mapping relationship.

```java
@Route("test")
public class TestActivity extends AppCompatActivity {
	...
}
```

Then you can just call `Router.build("test").go(context)` to open `TestActivity`, so cool! ​:clap:​​:clap:​​:clap:​

If you configured multiple route `@Route({"test","wtf"})`, both `test` and `wtf` can lead to `TestActivity`.

## Advanced usage

The whole api looks like this:  

```java
Router.build(uri)
	.extras(bundle)
	.requestCode(int)
	.anim(enter, exit)
	.addFlags(int)
	.callback(new RouteCallBack() {
        @Override
        public void succeed(Uri uri) {
            Log.i(TAG, "succeed: " + uri.toString());
        }

        @Override
        public void error(Uri uri, String message) {
            Log.w(TAG, "error: " + uri + ", " + message);
        }
    }).go(this);
```

Please refer to the [wiki](https://github.com/chenenyu/Router/wiki) for more informations.

## ProGuard

```Java
# Router
-keep class com.chenenyu.router.**
```

## Contact

QQ group: 271849001

## Other Libraries

[SuperAdater](https://github.com/byteam/SuperAdapter): Adapter knife(万能的Adapter).

[img-optimizer-gradle-plugin](https://github.com/chenenyu/img-optimizer-gradle-plugin)
: 一款用于优化png图片的gradle插件.

## License

[Apache 2.0](https://github.com/chenenyu/Router/blob/master/LICENSE)