## 2017.09.08

`router: 1.2.5`: 

1. [issues46](https://github.com/chenenyu/Router/issues/46)
2. [issues48](https://github.com/chenenyu/Router/issues/48)

## 2017.07.26

`router-gradle-plugin: 1.2.4`:

1. 删除`router-gradle-plugin`对Android Gradle Plugin的依赖，改善第一次接入`Router`时的编译时间。
2. 重构apt options的传递方式，更稳定完善，对kotlin项目更友好。

`router: 1.2.4`:

1. 修复获取QueryParameter时的bug(parameter中包含=号，会造成后面的内容被截断)。
2. 支持多parameter的情况，比如`http://example.com?user=Mike&user=Jane`，就可以通过`bundle.getStringArray("user")`获取到相关extra。

