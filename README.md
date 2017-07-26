[![Build Status](https://travis-ci.org/chenenyu/Router.svg?branch=master)](https://travis-ci.org/chenenyu/Router) ![license](https://img.shields.io/badge/license-Apache%202-yellow.svg) ![PullRequest](https://img.shields.io/badge/PRs-welcome-brightgreen.svg) 

# Router

建议浏览[中文wiki](https://github.com/chenenyu/Router/wiki).

![screenshot](static/screenshot.gif)

## Getting started

*  Add dependencies by adding the following lines to your top level `project/build.gradle`:  

```Groovy
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
    	...
        classpath 'com.chenenyu.router:gradle-plugin:1.2.4'
    }
}

// 可选配置.
ext {
    ...
    routerVersion = "x.y.z" // 指定特定的router版本
    compilerVersion = "x.y.z" // 指定特定的router-compiler版本
}
```

* Apply router plugin in your `module/build.gradle`:  

```  Groovy
apply plugin: 'com.android.application'
...
apply plugin: 'com.chenenyu.router'
```

current `router-gradle-plugin` version: ![Download](https://api.bintray.com/packages/chenenyu/maven/router-gradle-plugin/images/download.svg)

current `router` version: ![Download](https://api.bintray.com/packages/chenenyu/maven/router/images/download.svg)

current `router-compiler` version: ![compiler](https://api.bintray.com/packages/chenenyu/maven/router-compiler/images/download.svg)  

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

Please refer to the [wiki](https://github.com/chenenyu/Router/wiki) for more informations.

## ProGuard

See [wiki](https://github.com/chenenyu/Router/wiki).

## Contact

QQ group: 271849001

## License

[Apache 2.0](https://github.com/chenenyu/Router/blob/master/LICENSE)