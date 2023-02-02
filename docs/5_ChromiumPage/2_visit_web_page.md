`ChromiumPage`对象和`WebPage`对象的 d 模式都能控制浏览器访问网页。这里只对`ChromiumPage`进行说明，后面章节再单独介绍`WebPage`。

# ✔️ `get()`

该方法用于跳转到一个网址。当连接失败时，程序会进行重试。

| 参数名称          | 类型               | 默认值     | 说明                          |
|:-------------:|:----------------:|:-------:| --------------------------- |
| `url`         | `str`            | 必填      | 目标 url                      |
| `show_errmsg` | `bool`           | `False` | 连接出错时是否显示和抛出异常              |
| `retry`       | `int`            | `None`  | 重试次数，为`None`时使用页面参数，默认 3    |
| `interval`    | `int`<br>`float` | `None`  | 重试间隔（秒），为`None`时使用页面参数，默认 2 |
| `timeout`     | `int`<br>`float` | `None`  | 加载超时时间（秒）                   |

| 返回类型   | 说明    |
|:------:| ----- |
| `bool` | 否连接成功 |

**示例：**

```python
from DrissionPage import ChromiumPage

page = ChromiumPage()
page.get('https://www.baidu.com')
```

---

# ✔️ 设置超时和重试

网络不稳定时访问页面不一定成功，`get()`方法内置了超时和重试功能。通过`retry`、`interval`、`timeout`三个参数进行设置。  
其中，如不指定`timeout`参数，该参数会使用`ChromiumPage`的`timeouts`属性的`page_load`参数中的值。

```python
from DrissionPage import ChromiumPage

page = ChromiumPage()
page.get('https://www.163.com', retry=1, interval=1, timeout=1.5)
```

---

# ✔️ 设置加载策略

通过设置`ChromiumPage`对象的`page_load_strategy`属性，可设置页面停止加载的时机。页面加载时，在到达超时时间，或达到设定的状态，就会停止，可有效节省采集时间。有以下三种模式：

- `'normal'`：常规模式，会等待页面加载完毕

- `'eager'`：加载完 DOM 即停止加载

- `'none'`：完成连接即停止加载

默认设置为`'normal'`。

```python
from DrissionPage import ChromiumPage

page = ChromiumPage()
page.set_page_load_strategy('eager')
page.get('https://www.163.com')
```
