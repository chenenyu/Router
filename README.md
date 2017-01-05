[![Build Status](https://travis-ci.org/chenenyu/Router.svg?branch=master)](https://travis-ci.org/chenenyu/Router) ![license](https://img.shields.io/badge/license-Apache%202-yellow.svg) [![version](https://img.shields.io/github/release/chenenyu/Router.svg)]  (https://github.com/chenenyu/Router/releases) ![API](https://img.shields.io/badge/API-9%2B-orange.svg) ![PullRequest](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)

# Router

> A router library for Android paltform, featuring *simple* and *flexible*.

建议浏览[中文wiki](https://github.com/chenenyu/Router/wiki).

![screenshot](static/screenshot.gif)

## Getting started

*  Add dependencies by adding the following lines to your `app/build.gradle`:  
```Groovy
dependencies {
    annotationProcessor 'com.chenenyu.router:compiler:0.1.0'
    compile 'com.chenenyu.router:router:0.2.0'
}
```

If you're using a version of the Android gradle plugin below 2.2, see https://bitbucket.org/hvisser/android-apt.  

(Note: current `compiler` version: ![compiler](https://api.bintray.com/packages/chenenyu/maven/router-compiler/images/download.svg), current `router` version: ![Download](https://api.bintray.com/packages/chenenyu/maven/router/images/download.svg))

## Simple useage

`Router` uses annotation to specify the mapping relationship.

```java
@Route("test")
public class TestActivity extends AppCompatActivity {
	...
}
```

Then you can just call `Router.build("test").go(context)` to open `TestActivity`, so cool! ​:clap:​​:clap:​​:clap:​

If you configed multiple route `@Route({"test","wtf"})`, both `test` and `wtf` can lead to `TestActivity`.

## Advanced useage

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

Please refer to the [wiki](https://github.com/chenenyu/Router/wiki) for more infomations.

## License

[Apache 2.0](https://github.com/chenenyu/Router/blob/master/LICENSE)