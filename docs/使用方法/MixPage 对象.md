MixPage 页面对象封装了常用的网页操作，并实现 driver 和 session 模式之间的切换。  
MixPage 须控制一个 Drission 对象并使用其中的 driver 或 session，如没有传入，MixPage 会自己创建一个（使用传入的配置信息或从默认 ini 文件读取）。

Tips: 多对象协同工作时，可将一个 MixPage 中的 Drission 对象传递给另一个，使多个对象共享登录信息或操作同一个页面。

## 创建对象

创建对象方式有3种：简易、传入 Drission 对象、传入配置。可根据实际需要选择。

```python
# 简易创建方式，以 ini 文件默认配置自动创建 Drission 对象
page = MixPage()
page = MixPage('s')

# 以传入 Drission 对象创建
page = MixPage(drission=drission)
page = MixPage(mode='s', drission=drission, timeout=5)  # session 模式，等待时间5秒（默认10秒）

# 传入配置信息，MixPage 根据配置在内部创建 Drission
page = MixPage(driver_options=do, session_options=so)  # 默认 d 模式
```

## 访问网页

```python
# 默认方式
page.get(url)
page.post(url, data, **kwargs)  # 只有 session 模式才有 post 方法

# 指定重试次数和间隔
page.get(url, retry=5, interval=0.5)
```

Tips：若连接出错，程序会自动重试若干次，可指定重试次数和等待间隔。

## 切换模式

在 s 和 d 模式之间切换，切换时会自动同步 cookies 和正在访问的 url。

```python
page.change_mode(go=False)  # go 为 False 表示不跳转 url
```

Tips：使用某种模式独有的方法时会自动跳转到该模式。

## 页面属性

```python
page.url  # 当前访问的 url
page.mode  # 当前模式
page.drission  # 当前使用的 Dirssion 对象
page.driver  # 当前使用的 WebDirver 对象
page.session  # 当前使用的 Session 对象
page.cookies  # 获取 cookies 信息
page.html  # 页面源代码
page.json  # 当返回内容是json格式时，返回对应的字典
page.title  # 当前页面标题

# d 模式独有：
page.tabs_count  # 返回标签页数量
page.tab_handles  # 返回所有标签页 handle 列表
page.current_tab_num  # 返回当前标签页序号
page.current_tab_handle  # 返回当前标签页 handle
```

## 页面操作

调用只属于 d 模式的方法，会自动切换到 d 模式。详细用法见 APIs。

```python
page.set_cookies()  # 设置cookies
page.get_cookies()  # 获取 cookies，可以 list 或 dict 方式返回
page.change_mode()  # 切换模式，会自动复制 cookies
page.cookies_to_session()  # 从 WebDriver 对象复制 cookies 到 Session 对象
page.cookies_to_driver()  # 从 Session 对象复制 cookies 到 WebDriver 对象
page.get(url, retry, interval, **kwargs)  # 用 get 方式访问网页，可指定重试次数及间隔时间
page.ele(loc_or_ele, timeout)  # 获取符合条件的第一个元素、节点或属性
page.eles(loc_or_ele, timeout)  # 获取所有符合条件的元素、节点或属性
page.download(url, save_path, rename, file_exists, **kwargs)  # 下载文件
page.close_driver()  # 关闭 WebDriver 对象
page.close_session()  # 关闭 Session 对象

# s 模式独有：
page.post(url, data, retry, interval, **kwargs)  # 以 post 方式访问网页，可指定重试次数及间隔时间

# d 模式独有：
page.wait_ele(loc_or_ele, mode, timeout)  # 等待元素从 dom 删除、显示、隐藏
page.run_script(js, *args)  # 运行 js 语句
page.create_tab(url)  # 新建并定位到一个标签页,该标签页在最后面
page.to_tab(num_or_handle)  # 跳转到标签页
page.close_current_tab()  # 关闭当前标签页
page.close_other_tabs(num_or_handles)  # 关闭其它标签页
page.to_iframe(iframe)  # 切入 iframe
page.screenshot(path)  # 页面截图
page.scroll_to_see(element)  # 滚动直到某元素可见
page.scroll_to(mode, pixel)  # 按参数指示方式滚动页面，可选滚动方向：'top', 'bottom', 'rightmost', 'leftmost', 'up', 'down', 'left', 'right', 'half'
page.refresh()  # 刷新当前页面
page.back()  # 浏览器后退
page.et_window_size(x, y)  # 设置浏览器窗口大小，默认最大化
page.check_page()  # 检测页面是否符合预期
page.chrome_downloading()  # 获取 chrome 正在下载的文件列表
page.process_alert(mode, text)  # 处理提示框
```

## cookies 的使用

MixPage 支持获取和设置 cookies，具体使用方法如下：

```python
page.cookies  # 以字典形式返回 cookies，只会返回当前域名可用的 cookies
page.get_cookies(as_dict=False)  # 以列表形式返回当前域名可用 cookies，每个 cookie 包含所有详细信息
page.get_cookies(all_domains=True)  # 以列表形式返回所有 cookies，只有 s 模式有效
page.set_cookies(cookies)  # 设置 cookies，可传入 RequestsCookieJar, list, tuple, str, dict
```

Tips:

- d 模式设置 cookies 后要刷新页面才能看到效果。
- s 模式可在 ini 文件、SessionOptions、配置字典中设置 cookies，在 MixPage 初始化时即可传入，d 模式只能用 set_cookies() 函数设置。