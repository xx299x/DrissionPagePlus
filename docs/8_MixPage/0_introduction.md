本库 3.0 以前的版本是对 selenium 进行重新封装实现的。

其页面对象为`MixPage`和`DriverPage`，对应于新版的`WebPage`和`ChromiumPage`。

经过几年的使用，旧版已相当稳定。但由于依赖 selenium，功能开发受到较大制约，且 selenium 有其特征，容易被网站识别，故开发了新版`WebPage`取代之。

新版于旧版使用方法基本一致，但新版功能更多更强，且部分方法或属性名称有修改。

目前旧版开发已冻结，为兼容以前的项目，除了修复 bug，旧版不会再有功能上的修改。

有兴趣的读者可以了解一下。

其结构图如下：

![](https://gitee.com/g1879/DrissionPage/raw/master/docs/imgs/mixpage.jpg)
