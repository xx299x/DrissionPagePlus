# ✔️ 运行环境

操作系统：Windows、Linux 或 Mac。

python 版本：3.6 及以上

支持浏览器：Chromium 内核（如 Chrome 和 Edge）

---

# ✔️ 安装

请使用 pip 安装 DrissionPage：

```console
pip install DrissionPage
```

---

# ✔️ 升级

```console
pip install DrissionPage --upgrade
```

---

# ✔️ 导入

## 📍 页面类

页面类用于控制浏览器，或收发数据包，是最主要的工具。DrissionPage 包含三种主要页面类。根据须要在其中选择使用。

`WebPage`是功能最全面的页面类，既可控制浏览器，也可收发数据包：

```python
from DrissionPage import WebPage
```

如果只要控制浏览器，导入`ChromiumPage`：

```python
from DrissionPage import ChromiumPage
```

如果只要收发数据包，导入`SessionPage`：

```python
from DrissionPage import SessionPage
```

---

## 📍 配置类

很多时候我们须要设置启动参数，可导入以下两个类，但不是必须的。

`ChromiumOptions`类用于设置浏览器启动参数：

```python
from DrissionPage import ChromiumOptions
```

`SessionOptions`类用于设置`Session`对象启动参数：

```
from DrissionPage import SessionOptions
```

---

## 📍 其它工具

有两个我们可能须要用到的工具，需要时可以导入。

动作链，用于模拟一系列键盘和鼠标的操作：

```python
from DrissionPage import ActionChains
```

键盘按键类，用于键入 ctrl、alt 等按键：

```python
from DrissionPage import Keys
```

`easy_set`里保存了一些便捷的 ini 文件设置方法，可选择使用：

```python
from DrissionPage.easy_set import *
```

---

## 📍 旧版页面和配置类

旧版`MixPage`是基于 selenium 封装而成，使用方法与`WebPage`一致。

```python
from DrissionPage import MixPage
```

旧版配置类：

```python
from DrissionPage import DriverOptions
from DrissionPage import Drission
```
