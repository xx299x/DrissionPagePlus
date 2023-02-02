`SessionPage`对象和`WebPage`对象的 s 模式都能收发数据包，本节只介绍`SessionPage`的创建，在`WebPage`的章节再对其进行介绍。

# ✔️ `SessionPage`初始化参数

`SessionPage`对象是 3 种页面对象中最简单的。

| 初始化参数                | 类型                            | 默认值    | 说明                                                              |
|:--------------------:|:-----------------------------:|:------:| --------------------------------------------------------------- |
| `session_or_options` | `Session`<br>`SessionOptions` | `None` | 传入`Session`对象时使用该对象收发数据包；传入`SessionOptions`对象时用该配置创建`Session`对象 |
| `timeout`            | `float`                       | `None` | 连接超时时间，为`None`则从配置文件中读取                                         |

---

# ✔️ 直接创建

这种方式代码最简洁，程序会从配置文件中读取配置，自动生成页面对象。

```python
from DrissionPage import SessionPage

page = SessionPage()
```

`SessionPage`无须控制浏览器，无须做任何配置即可使用。

!>**注意：**<br>这种方式的程序不能直接打包，因为使用到 ini 文件。可参考“打包程序”一节的方法。

---

# ✔️ 通过配置信息创建

如果须要在使用前进行一些配置，可使用`SessionOptions`。它是专门用于设置`Session`对象初始状态的类，内置了常用的配置。详细使用方法见“启动配置”一节。

## 📍 使用方法

在`SessionPage`创建时，将已创建和设置好的`SessionOptions`对象以参数形式传递进去即可。

| 初始化参数       | 类型     | 默认值    | 说明                       |
| ----------- | ------ | ------ | ------------------------ |
| `read_file` | `bool` | `True` | 是否从 ini 文件中读取配置信息        |
| `ini_path`  | `str`  | `None` | 文件路径，为`None`则读取默认 ini 文件 |

!>**注意：**<br> `Session`对象创建后再修改这个配置是没有效果的。

```python
# 导入 SessionOptions
from DrissionPage import SessionPage, SessionOptions

proxies = {'http': 'http://127.0.0.1:1080',
           'https': 'http://127.0.0.1:1080'}

# 创建配置对象，并设置代理信息
so = SessionOptions().set_proxies(proxies)
# 用该配置创建页面对象
page = SessionPage(session_or_options=so)
```

?>**Tips：**<br>您可以把配置保存到配置文件以后自动读取，详见”启动配置“章节。

---

## 📍 从指定 ini 文件创建

以上方法是使用默认 ini 文件中保存的配置信息创建对象，你可以保存一个 ini 文件到别的地方，并在创建对象时指定使用它。

```python
from DrissionPage import SessionPage, SessionOptinos

# 创建配置对象时指定要读取的ini文件路径
so = SessionOptinos(ini_path=r'./config1.ini')
# 使用该配置对象创建页面
page = SessionPage(session_or_options=so)
```

---

# ✔️ 传递控制权

当须要使用多个页面对象共同操作一个页面时，可在页面对象创建时接收另一个页面间对象传递过来的`Session`对象，以达到多个页面对象同时使用一个`Session`对象的效果。

```python
# 创建一个页面
page1 = SessionPage()
# 获取页面对象内置的Session对象
session = page1.session
# 在第二个页面对象初始化时传递该对象
page2 = SessionPage(session_or_options=session)
```
