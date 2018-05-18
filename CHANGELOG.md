## 2018.05.18

`router:1.4.2`:

1. Ordered interceptors.

## 2018.04.03

`router:1.4.1`:

1. fix ClassCastException in muilti-thread condition

## 2018.04.03

`router:1.4.0`:

1. [issue76](https://github.com/chenenyu/Router/issues/76)
2. Refactor `RouteInterceptor`
3. Remove Method-Callable support

## 2018.02.06

`router:1.4.0-beta1` `compiler:1.4.0-beta1` `annotation:0.4.0`:

1. fix: https://github.com/chenenyu/Router/pull/71
2. ~~Router can annotate methods now!~~ (Removed in 1.4.0)

## 2018.01.30

`router:1.3.3`:

1. fix: https://github.com/chenenyu/Router/pull/68

## 2017.11.29

`router:1.3.2`:

1. 兼容低版本的v4包. [issue59](https://github.com/chenenyu/Router/issues/59)

`compiler:1.3.2`:

1. 编译时检查是否存在重复的注解. [issue60](https://github.com/chenenyu/Router/issues/60)

## 2017.11.09

`router:1.3.0`:

1. 删除gradle-plugin,采用新的集成方式.
2. 新的初始化方式.

## 2017.09.30

`router:1.2.6`:

1. Fix bug in `AbsImplicitMatcher`.
2. Add some api for Intent.

## 2017.09.12

`router-gradle-plugin:1.2.5.1`:

Bug fix for [issues51](https://github.com/chenenyu/Router/issues/51) in `1.2.5`.

## 2017.09.08

`router:1.2.5`: 

1. [issues46](https://github.com/chenenyu/Router/issues/46)
2. [issues48](https://github.com/chenenyu/Router/issues/48)

## 2017.07.26

`router-gradle-plugin:1.2.4`:

1. 删除`router-gradle-plugin`对Android Gradle Plugin的依赖，改善第一次接入`Router`时的编译时间。
2. 重构apt options的传递方式，更稳定完善，对kotlin项目更友好。

`router: 1.2.4`:

1. 修复获取QueryParameter时的bug(parameter中包含=号，会造成后面的内容被截断)。
2. 支持多parameter的情况，比如`http://example.com?user=Mike&user=Jane`，就可以通过`bundle.getStringArray("user")`获取到相关extra。

