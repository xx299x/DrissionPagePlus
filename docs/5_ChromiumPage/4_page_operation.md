本节介绍浏览器页面交互功能，元素的交互在下一节。

页面对象包括`ChromiumPage`、d 模式的`WebPage`、`ChromiumTab`、`ChromiumFrame`几种，这里只介绍`ChromiumPage`，其它几种后面专门章节介绍。

# ✔️ 页面跳转

## 📍 `get()`

该方法用于跳转到一个网址。当连接失败时，程序会进行重试。

| 参数名称          | 类型               | 默认值     | 说明                          |
|:-------------:|:----------------:|:-------:| --------------------------- |
| `url`         | `str`            | 必填      | 目标 url                      |
| `show_errmsg` | `bool`           | `False` | 连接出错时是否显示和抛出异常              |
| `retry`       | `int`            | `None`  | 重试次数，为`None`时使用页面参数，默认 3    |
| `interval`    | `int`<br>`float` | `None`  | 重试间隔（秒），为`None`时使用页面参数，默认 2 |
| `timeout`     | `int`<br>`float` | `None`  | 加载超时时间（秒）                   |

| 返回类型   | 说明     |
|:------:| ------ |
| `bool` | 是否连接成功 |

**示例：**

```python
page.get('https://www.baidu.com')
```

---

## 📍 `back()`

此方法用于在浏览历史中后退若干步。

| 参数名称    | 类型    | 默认值 | 说明   |
|:-------:|:-----:|:---:| ---- |
| `steps` | `int` | `1` | 后退步数 |

**返回：**`None`

**示例：**

```python
page.back(2)  # 后退两个网页
```

---

## 📍 `forward()`

此方法用于在浏览历史中前进若干步。

| 参数名称    | 类型    | 默认值 | 说明   |
|:-------:|:-----:|:---:| ---- |
| `steps` | `int` | `1` | 前进步数 |

**返回：**`None`

```python
page.forward(2)  # 前进两步
```

---

## 📍 `refresh()`

此方法用于刷新当前页面。

| 参数名称           | 类型     | 默认值     | 说明        |
|:--------------:|:------:|:-------:| --------- |
| `ignore_cache` | `bool` | `False` | 刷新时是否忽略缓存 |

**返回：**`None`

```python
page.refresh()  # 刷新页面
```

---

## 📍 `stop_loading()`

此方法用于强制停止当前页面加载。

**参数：** 无

**返回：**`None`

---

## 📍 `wait_loading()`

此方法用于等待页面进入加载状态。

我们经常会通过点击页面元素进入下一个网页，并立刻获取新页面的元素。但若跳转前的页面拥有和跳转后页面相同定位符的元素，会导致过早获取元素，跳转后失效的问题。使用此方法，会阻塞程序，等待页面开始加载后再继续，从而避免上述问题。

| 参数名称      | 类型                                   | 默认值    | 说明                                                |
|:---------:|:------------------------------------:|:------:| ------------------------------------------------- |
| `timeout` | `int`<br>`float`<br>`None`<br>`True` | `None` | 超时时间，为`None`或`Ture`时使用页面`timeout`设置<br>为数字时等待相应时间 |

| 返回类型   | 说明            |
|:------:| ------------- |
| `bool` | 等待结束时是否进入加载状态 |

**示例：**

```python
ele.click()  # 点击某个元素
page.wait_loading()  # 等待页面进入加载状态
# 执行在新页面的操作
print(page.title)
```

---

# ✔️ 执行脚本或命令

## 📍 `run_js()`

此方法用于执行 js 脚本。

| 参数名称      | 类型     | 默认值     | 说明                                              |
|:---------:|:------:|:-------:| ----------------------------------------------- |
| `script`  | `str`  | 必填      | js 脚本文本                                         |
| `as_expr` | `bool` | `False` | 是否作为表达式运行，为`True`时`args`参数无效                    |
| `*args`   | -      | 无       | 传入的参数，按顺序在js文本中对应`argument[0]`、`argument[1]`... |

| 返回类型  | 说明     |
|:-----:| ------ |
| `Any` | 脚本执行结果 |

**示例：**

```python
# 用传入参数的方式执行 js 脚本显示弹出框显示 Hello world!
page.run_js('alert(arguments[0]+arguments[1]);', 'Hello', ' world!')
```

---

## 📍 `run_async_script()`

此方法用于以异步方式执行 js 代码。

**参数：**

| 参数名称      | 类型     | 默认值     | 说明                                              |
|:---------:|:------:|:-------:| ----------------------------------------------- |
| `script`  | `str`  | 必填      | js 脚本文本                                         |
| `as_expr` | `bool` | `False` | 是否作为表达式运行，为`True`时`args`参数无效                    |
| `*args`   | -      | 无       | 传入的参数，按顺序在js文本中对应`argument[0]`、`argument[1]`... |

**返回：**`None`

---

## 📍 `run_cdp()`

此方法用于执行 Chrome DevTools Protocol 语句。

cdp 用法详见 [Chrome DevTools Protocol](https://chromedevtools.github.io/devtools-protocol/)。

| 参数名称         | 类型    | 默认值 | 说明   |
|:------------:|:-----:|:---:| ---- |
| `cmd`        | `str` | 必填  | 协议项目 |
| `**cmd_args` | -     | 无   | 项目参数 |

| 返回类型   | 说明      |
|:------:| ------- |
| `dict` | 执行返回的结果 |

**示例：**

```python
# 停止页面加载
page.run_cdp('Page.stopLoading')
```

---

# ✔️ cookies 及缓存

## 📍 `set_cookies()`

此方法用于设置 cookies。

可以接收`CookieJar`、`list`、`tuple`、`str`、`dict`格式的 cookies。

| 参数名称      | 类型                                                          | 默认值 | 说明         |
|:---------:|:-----------------------------------------------------------:|:---:| ---------- |
| `cookies` | `RequestsCookieJar`<br>`list`<br>`tuple`<br>`str`<br>`dict` | 必填  | cookies 信息 |

**返回：**`None`

```python
cookies = {'name': 'abc'}
page.set_cookies(cookies, set_session=True, set_driver=True)
```

---

## 📍 `set_session_storage()`

此方法用于设置或删除某项 sessionStorage 信息。

| 参数名称    | 类型               | 默认值 | 说明             |
|:-------:|:----------------:|:---:| -------------- |
| `item`  | `str`            | 必填  | 要设置的项          |
| `value` | `str`<br>`False` | 必填  | 为`False`时，删除该项 |

**返回：**`None`

**示例：**

```python
page.set_session_storage(item='abc', value='123')
```

---

## 📍 `set_local_storage()`

此方法用于设置或删除某项 localStorage 信息。

| 参数名称    | 类型               | 默认值 | 说明             |
|:-------:|:----------------:|:---:| -------------- |
| `item`  | `str`            | 必填  | 要设置的项          |
| `value` | `str`<br>`False` | 必填  | 为`False`时，删除该项 |

**返回：**`None`

---

## 📍 `clear_cache()`

此方法用于清除缓存，可选择要清除的项。

| 参数名称              | 类型     | 默认值    | 说明                  |
|:-----------------:|:------:|:------:| ------------------- |
| `session_storage` | `bool` | `True` | 是否清除 sessionstorage |
| `local_storage`   | `bool` | `True` | 是否清除 localStorage   |
| `cache`           | `bool` | `True` | 是否清除 cache          |
| `cookies`         | `bool` | `True` | 是否清除 cookies        |

**返回：**`None`

**示例：**

```python
page.clear_cache(cookies=False)  # 除了 cookies，其它都清除
```

---

# ✔️ 各种设置

## 📍 `set_timeouts()`

此方法用于设置三种超时时间，单位为秒。可单独设置，为`None`表示不改变原来设置。

| 参数名称        | 类型                         | 默认值    | 说明       |
|:-----------:|:--------------------------:|:------:| -------- |
| `implicit`  | `int`<br>`float`<br>`None` | `None` | 整体超时时间   |
| `page_load` | `int`<br>`float`<br>`None` | `None` | 页面加载超时时间 |
| `script`    | `int`<br>`float`<br>`None` | `None` | 脚本运行超时时间 |

**返回：**`None`

**示例：**

```python
page.set_timeouts(implicit=10, page_load=30)
```

---

## 📍 `set_page_load_strategy`

此属性用于设置页面加载策略，调用其方法选择某种策略。

| 方法名称       | 参数  | 说明                  |
|:----------:|:---:| ------------------- |
| `normal()` | 无   | 等待页面完全加载完成，为默认状态    |
| `eager()`  | 无   | 等待文档加载完成就结束，不等待资源加载 |
| `none()`   | 无   | 页面连接完成就结束           |

**示例：**

```python
page.set_page_load_strategy.normal()
page.set_page_load_strategy.eager()
page.set_page_load_strategy.none()
```

---

## 📍 `set_ua_to_tab()`

此方法用于为浏览器当前标签页设置 user agent，只在当前 tab 有效。

| 参数名称 | 类型    | 默认值 | 说明             |
|:----:|:-----:|:---:| -------------- |
| `ua` | `str` | 必填  | user agent 字符串 |

**返回：**`None`

---

## 📍 `set_headers()`

此方法用于设置额外添加到当前页面请求 headers 的参数。

| 参数名称      | 类型     | 默认值 | 说明                |
|:---------:|:------:|:---:| ----------------- |
| `headers` | `dict` | 必填  | `dict`形式的 headers |

**返回：**`None`

**示例：**

```python
h = {'connection': 'keep-alive', 'accept-charset': 'GB2312,utf-8;q=0.7,*;q=0.7'}
page.set_headers(headers=h)
```

---

# ✔️ 窗口管理

## 📍 调整大小和位置

`set_window`属性返回一个`WindowSetter`对象，调用其方法执行改变窗口状态。

| 方法                    | 参数               | 说明   |
|:---------------------:|:----------------:| ---- |
| `maximized()`         | 无                | 最大化  |
| `minimized()`         | 无                | 最小化  |
| `fullscreen()`        | 无                | 全屏   |
| `normal()`            | 无                | 常规   |
| `size(width, height)` | 宽，高              | 设置大小 |
| `location(x, y)`      | 屏幕坐标，左上角为 (0, 0) | 设置位置 |

```python
# 窗口最大化
page.set_window.maximized()

# 窗口全屏，即 F11
page.set_window.fullscreen()

# 恢复普通窗口
page.set_window.normal()

# 设置窗口大小
page.set_window.size(500, 500)

# 设置窗口位置
page.set_window.location(200, 200)
```

---

## 📍 隐藏和显示窗口

`hide_browser()`和`show_browser()`方法用于随时隐藏和显示浏览器窗口。

与 headless 模式不一样，这两个方法是直接隐藏和显示浏览器进程。在任务栏上也会消失。

只支持 Windows 系统，并且必须已安装 pypiwin32 库才可使用。

🔸 `hide_browser()`

此方法用于隐藏当前浏览器窗口。

**参数：** 无

**返回：**`None`

---

🔸 `show_browser()`

此方法用于显示当前浏览器窗口。

**参数：** 无

**返回：**`None`

**示例：**

```python
page.hide_browser()
```

!>**注意：**<br>- 浏览器隐藏后并没有关闭，下次运行程序还会接管已隐藏的浏览器<br>- 浏览器隐藏后，如果有新建标签页，会自行显示出来

---

# ✔️ 滚动页面

## 📍 `scroll`

此属性用于以某种方式滚动页面。  
调用时返回一个`Scroll`对象，调用其方法实现页面各种方式的滚动。

| 方法                  | 参数                  | 功能               |
| ------------------- | ------------------- | ---------------- |
| `to_top()`          | 无                   | 滚动到顶端，水平位置不变     |
| `to_bottom()`       | 无                   | 滚动到底端，水平位置不变     |
| `to_half()`         | 无                   | 滚动到垂直中间位置，水平位置不变 |
| `to_rightmost()`    | 无                   | 滚动到最右边，垂直位置不变    |
| `to_leftmost()`     | 无                   | 滚动到最左边，垂直位置不变    |
| `to_location(x, y)` | 滚动条坐标值，左上角为 (0 , 0) | 滚动到指定位置          |
| `up(pixel)`         | 滚动的像素               | 向上滚动若干像素，水平位置不变  |
| `down(pixel)`       | 滚动的像素               | 向下滚动若干像素，水平位置不变  |
| `right(pixel)`      | 滚动的像素               | 向左滚动若干像素，垂直位置不变  |
| `left(pixel)`       | 滚动的像素               | 向右滚动若干像素，垂直位置不变  |

```python
# 页面滚动到底部
page.scroll.to_bottom()

# 页面滚动到最右边
page.scroll.to_rightmost()

# 页面向下滚动 200 像素
page.scroll.down(200)

# 滚动到指定位置
page.scroll.to_location(100, 300)
```

---

## 📍 `scroll_to_see()`

此方法用于滚动页面直到元素可见。

| 参数名称         | 类型                                    | 默认值 | 说明                |
|:------------:|:-------------------------------------:|:---:| ----------------- |
| `loc_or_ele` | `str`<br>`tuple`<br>`ChromiumElement` | 必填  | 元素的定位信息，可以是元素、定位符 |

**返回：**`None`

**示例：**

```python
# 滚动到某个已获取到的元素
ele = page.ele('tag:div')
page.scroll_to_see(ele)

# 滚动到按定位符查找到的元素
page.scroll_to_see('tag:div')
```

---

# ✔️ 处理弹出消息

## 📍 `handle_alert()`

此方法 用于处理提示框。  
它能够设置等待时间，等待提示框出现才进行处理，若超时没等到提示框，返回`None`。  
也可只获取提示框文本而不处理提示框。

| 参数名称      | 类型               | 默认值    | 说明                                         |
|:---------:|:----------------:|:------:| ------------------------------------------ |
| `accept`  | `bool`<br>`None` | `True` | `True`表示确认，`False`表示取消，`None`不会按按钮但依然返回文本值 |
| `send`    | `str`            | `None` | 处理 prompt 提示框时可输入文本                        |
| `timeout` | `int`<br>`float` | `None` | 等待提示框出现的超时时间，为`None`时使用页面整体超时时间            |

| 返回类型   | 说明              |
|:------:| --------------- |
| `str`  | 提示框内容文本         |
| `None` | 未等到提示框则返回`None` |

**示例：**

```python
# 确认提示框并获取提示框文本
txt = page.handle_alert()

# 点击取消
page.handle_alert(accept=False)

# 给 prompt 提示框输入文本并点击确定
paeg.handle_alert(accept=True, send='some text')

# 不处理提示框，只获取提示框文本
txt = page.handle_alert(accept=None)
```

---

# ✔️ 关闭浏览器

## 📍 `quit()`

此方法用于关闭浏览器。

**参数：** 无

**返回：**`**None`

```python
page.quit()
```
